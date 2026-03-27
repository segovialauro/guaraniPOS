package com.guarani.pos.pagare.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.cash.model.CashMovement;
import com.guarani.pos.cash.model.CashMovementType;
import com.guarani.pos.cash.model.CashSession;
import com.guarani.pos.cash.repository.CashMovementRepository;
import com.guarani.pos.cash.repository.CashSessionRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.customer.model.Customer;
import com.guarani.pos.customer.repository.CustomerRepository;
import com.guarani.pos.pagare.dto.PagareCreateRequest;
import com.guarani.pos.pagare.dto.PagarePaymentRequest;
import com.guarani.pos.pagare.dto.PagarePaymentResponse;
import com.guarani.pos.pagare.dto.PagareResponse;
import com.guarani.pos.pagare.model.Pagare;
import com.guarani.pos.pagare.model.PagarePago;
import com.guarani.pos.pagare.repository.PagareRepository;
import com.guarani.pos.sale.model.Sale;
import com.guarani.pos.sale.repository.SaleRepository;

@Service
public class PagareService {

    private final PagareRepository pagareRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;

    public PagareService(PagareRepository pagareRepository,
                         CompanyRepository companyRepository,
                         CustomerRepository customerRepository,
                         SaleRepository saleRepository,
                         UserRepository userRepository,
                         CashSessionRepository cashSessionRepository,
                         CashMovementRepository cashMovementRepository) {
        this.pagareRepository = pagareRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.userRepository = userRepository;
        this.cashSessionRepository = cashSessionRepository;
        this.cashMovementRepository = cashMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<PagareResponse> findRecent(Long companyId) {
        updateOverdueStatuses(companyId);

        return pagareRepository.findTop20ByCompanyIdOrderByFechaEmisionDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PagareResponse> findAuditHistory(Long companyId, String from, String to, String status, String q) {
        updateOverdueStatuses(companyId);

        LocalDate fromDate = from != null && !from.isBlank() ? LocalDate.parse(from) : LocalDate.of(2000, 1, 1);
        LocalDate toDate = to != null && !to.isBlank() ? LocalDate.parse(to) : LocalDate.of(2999, 12, 31);
        String normalizedStatus = status != null && !status.isBlank()
                ? status.trim().toUpperCase(Locale.ROOT)
                : "";
        String queryPattern = q != null && !q.isBlank()
                ? "%" + q.trim().toUpperCase(Locale.ROOT) + "%"
                : "";

        return pagareRepository.search(companyId, fromDate, toDate, normalizedStatus, queryPattern)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PagareResponse create(Long companyId, Long userId, PagareCreateRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        Customer customer = customerRepository.findByIdAndCompanyId(request.customerId(), companyId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Sale sale = null;
        if (request.saleId() != null) {
            sale = saleRepository.findById(request.saleId())
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada."));
        }

        Pagare pagare = new Pagare();
        pagare.setCompany(company);
        pagare.setCustomer(customer);
        pagare.setSale(sale);
        pagare.setCreatedBy(user);
        pagare.setNumeroPagare(generatePagareNumber(companyId));
        pagare.setFechaEmision(request.issueDate());
        pagare.setFechaVencimiento(request.dueDate());
        pagare.setMonto(request.amount());
        pagare.setSaldo(request.amount());
        pagare.setEstado(resolveInitialStatus(request.dueDate(), request.amount()));
        pagare.setObservacion(request.observation());

        return toResponse(pagareRepository.save(pagare));
    }

    @Transactional
    public PagareResponse registerPayment(Long companyId, Long userId, Long pagareId, PagarePaymentRequest request) {
        Pagare pagare = pagareRepository.findByIdAndCompanyId(pagareId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Pagare no encontrado."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if ("PAGADO".equalsIgnoreCase(pagare.getEstado())) {
            throw new IllegalArgumentException("El pagare ya esta pagado.");
        }

        if ("ANULADO".equalsIgnoreCase(pagare.getEstado())) {
            throw new IllegalArgumentException("El pagare esta anulado.");
        }

        if (request.amount().compareTo(pagare.getSaldo()) > 0) {
            throw new IllegalArgumentException("El pago no puede superar el saldo pendiente.");
        }

        PagarePago pago = new PagarePago();
        pago.setPagare(pagare);
        pago.setMonto(request.amount());
        pago.setMetodoPago(request.paymentMethod());
        pago.setObservacion(request.observation());

        pagare.getPagos().add(pago);
        pagare.setSaldo(pagare.getSaldo().subtract(request.amount()));

        registerCashIncomeIfNeeded(
                companyId,
                user,
                request.amount(),
                request.paymentMethod(),
                "Cobro de pagare " + pagare.getNumeroPagare() + " - " + pagare.getCustomer().getNombre()
        );

        if (pagare.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
            pagare.setEstado("PAGADO");
        } else {
            pagare.setEstado("PARCIAL");
        }

        return toResponse(pagareRepository.save(pagare));
    }

    private void registerCashIncomeIfNeeded(Long companyId,
                                            User user,
                                            BigDecimal amount,
                                            String paymentMethod,
                                            String description) {
        if (!"EFECTIVO".equalsIgnoreCase(paymentMethod) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CashSession cashSession = cashSessionRepository
                .findFirstByCompany_IdAndUser_IdAndEstadoOrderByOpenedAtDesc(companyId, user.getId(), "ABIERTA")
                .orElseGet(() -> cashSessionRepository
                        .findFirstByCompany_IdAndEstadoOrderByOpenedAtDesc(companyId, "ABIERTA")
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Debe existir una caja abierta para registrar cobros de pagares en efectivo."
                        )));

        CashMovement movement = new CashMovement();
        movement.setCashSession(cashSession);
        movement.setCompany(cashSession.getCompany());
        movement.setUser(user);
        movement.setType(CashMovementType.INGRESO);
        movement.setAmount(amount);
        movement.setDescription(description);
        cashMovementRepository.save(movement);
    }

    private void updateOverdueStatuses(Long companyId) {
        LocalDate today = LocalDate.now();

        var pagares = pagareRepository.findTop20ByCompanyIdOrderByFechaEmisionDesc(companyId);
        for (Pagare pagare : pagares) {
            if (("PENDIENTE".equals(pagare.getEstado()) || "PARCIAL".equals(pagare.getEstado()))
                    && pagare.getFechaVencimiento().isBefore(today)
                    && pagare.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
                pagare.setEstado("VENCIDO");
            }
        }
    }

    private String generatePagareNumber(Long companyId) {
        long next = pagareRepository.countByCompanyId(companyId) + 1;
        String period = YearMonth.now().toString().replace("-", "");
        return "PG-" + period + "-" + new DecimalFormat("000000").format(next);
    }

    private String resolveInitialStatus(LocalDate dueDate, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "PAGADO";
        }
        if (dueDate.isBefore(LocalDate.now())) {
            return "VENCIDO";
        }
        return "PENDIENTE";
    }

    private PagareResponse toResponse(Pagare pagare) {
        return new PagareResponse(
                pagare.getId(),
                pagare.getNumeroPagare(),
                pagare.getCustomer().getId(),
                pagare.getCustomer().getNombre(),
                pagare.getSale() != null ? pagare.getSale().getId() : null,
                pagare.getFechaEmision(),
                pagare.getFechaVencimiento(),
                pagare.getMonto(),
                pagare.getSaldo(),
                pagare.getEstado(),
                pagare.getObservacion(),
                pagare.getPagos().stream()
                        .map(p -> new PagarePaymentResponse(
                                p.getId(),
                                p.getFechaPago(),
                                p.getMonto(),
                                p.getMetodoPago(),
                                p.getObservacion()
                        ))
                        .toList()
        );
    }
}

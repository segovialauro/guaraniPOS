package com.guarani.pos.budget.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.budget.dto.BudgetCreateRequest;
import com.guarani.pos.budget.dto.BudgetDetailResponse;
import com.guarani.pos.budget.dto.BudgetItemRequest;
import com.guarani.pos.budget.dto.BudgetResponse;
import com.guarani.pos.budget.model.Budget;
import com.guarani.pos.budget.model.BudgetDetail;
import com.guarani.pos.budget.repository.BudgetRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.customer.model.Customer;
import com.guarani.pos.customer.repository.CustomerRepository;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import com.guarani.pos.sale.dto.SaleCreateRequest;
import com.guarani.pos.sale.dto.SaleItemRequest;
import com.guarani.pos.sale.dto.SalePaymentRequest;
import com.guarani.pos.sale.dto.SaleResponse;
import com.guarani.pos.sale.model.Sale;
import com.guarani.pos.sale.repository.SaleRepository;
import com.guarani.pos.sale.service.SaleService;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SaleService saleService;
    private final SaleRepository saleRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         ProductRepository productRepository,
                         CustomerRepository customerRepository,
                         CompanyRepository companyRepository,
                         UserRepository userRepository,
                         SaleService saleService,
                         SaleRepository saleRepository) {
        this.budgetRepository = budgetRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.saleService = saleService;
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> findRecent(Long companyId) {
        return budgetRepository.findTop20ByCompanyIdOrderByFechaDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BudgetResponse create(Long companyId, Long userId, BudgetCreateRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findByIdAndCompanyId(request.customerId(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
        }

        Budget budget = new Budget();
        budget.setCompany(company);
        budget.setCustomer(customer);
        budget.setCreatedBy(user);
        budget.setEstado("PENDIENTE");
        budget.setObservacion(request.observation());
        budget.setVigenciaHasta(request.validUntil());
        budget.setNumeroPresupuesto(generateBudgetNumber(companyId));

        BigDecimal subtotalBeforeDiscounts = BigDecimal.ZERO;
        BigDecimal lineDiscountTotal = BigDecimal.ZERO;

        for (BudgetItemRequest item : request.items()) {
            Product product = productRepository.findByIdAndCompanyId(item.productId(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productId()));

            if (!product.isActivo()) {
                throw new IllegalArgumentException("El producto está inactivo: " + product.getNombre());
            }

            BigDecimal unitPrice = resolveBudgetUnitPrice(product, item.quantity());
            BigDecimal grossSubtotal = unitPrice.multiply(item.quantity());
            BigDecimal lineDiscount = sanitizeDiscountAmount(item.discountAmount(), grossSubtotal);
            BigDecimal subtotal = grossSubtotal.subtract(lineDiscount);

            BudgetDetail detail = new BudgetDetail();
            detail.setProduct(product);
            detail.setProductoCodigo(product.getCodigo());
            detail.setProductoNombre(product.getNombre());
            detail.setCantidad(item.quantity());
            detail.setPrecioUnitario(unitPrice);
            detail.setGrossSubtotal(grossSubtotal);
            detail.setDiscountAmount(lineDiscount);
            detail.setSubtotal(subtotal);

            budget.addDetail(detail);
            subtotalBeforeDiscounts = subtotalBeforeDiscounts.add(grossSubtotal);
            lineDiscountTotal = lineDiscountTotal.add(lineDiscount);
        }

        BigDecimal globalDiscountAmount = sanitizeDiscountAmount(
                request.globalDiscountAmount(),
                subtotalBeforeDiscounts.subtract(lineDiscountTotal)
        );
        BigDecimal total = subtotalBeforeDiscounts
                .subtract(lineDiscountTotal)
                .subtract(globalDiscountAmount);

        budget.setSubtotalBeforeDiscounts(subtotalBeforeDiscounts);
        budget.setGlobalDiscountAmount(globalDiscountAmount);
        budget.setDiscountTotal(lineDiscountTotal.add(globalDiscountAmount));
        budget.setTotal(total);

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    private String generateBudgetNumber(Long companyId) {
        long next = budgetRepository.countByCompanyId(companyId) + 1;
        String period = YearMonth.now().toString().replace("-", "");
        return "P-" + period + "-" + new DecimalFormat("000000").format(next);
    }

    private BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getNumeroPresupuesto(),
                budget.getFecha(),
                budget.getVigenciaHasta(),
                budget.getCustomer() != null ? budget.getCustomer().getId() : null,
                budget.getCustomer() != null ? budget.getCustomer().getNombre() : null,
                budget.getEstado(),
                budget.getObservacion(),
                budget.getConvertedSale() != null ? budget.getConvertedSale().getId() : null,
                budget.getSubtotalBeforeDiscounts(),
                budget.getDiscountTotal(),
                budget.getGlobalDiscountAmount(),
                budget.getTotal(),
                budget.getDetails().stream()
                        .map(d -> new BudgetDetailResponse(
                                d.getProduct().getId(),
                                d.getProductoCodigo(),
                                d.getProductoNombre(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getGrossSubtotal(),
                                d.getDiscountAmount(),
                                d.getSubtotal()
                        ))
                        .toList()
        );
    }
    
    @Transactional
    public BudgetResponse changeStatus(Long companyId, Long id, String status) {
        Budget budget = budgetRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado."));

        if (!List.of("PENDIENTE", "ENVIADO", "APROBADO", "RECHAZADO").contains(status)) {
            throw new IllegalArgumentException("Estado no permitido.");
        }

        if ("CONVERTIDO".equalsIgnoreCase(budget.getEstado())) {
            throw new IllegalArgumentException("El presupuesto ya fue convertido.");
        }

        budget.setEstado(status);
        return toResponse(budgetRepository.save(budget));
    }
    
    @Transactional
    public Long convertToSale(Long companyId, Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndCompanyId(budgetId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado."));

        if (!List.of("PENDIENTE", "ENVIADO", "APROBADO").contains(budget.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden convertir presupuestos pendientes, enviados o aprobados.");
        }

        if (budget.getVigenciaHasta() != null && budget.getVigenciaHasta().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("El presupuesto ya vencio y no puede convertirse en venta.");
        }

        SaleCreateRequest saleRequest = new SaleCreateRequest(
                budget.getCustomer() != null ? budget.getCustomer().getId() : null,
                "EFECTIVO",
                budget.getTotal(),
                budget.getGlobalDiscountAmount(),
                "Generada desde presupuesto " + budget.getNumeroPresupuesto(),
                List.of(new SalePaymentRequest("EFECTIVO", budget.getTotal(), "Presupuesto " + budget.getNumeroPresupuesto())),
                budget.getDetails().stream()
                        .map(detail -> new SaleItemRequest(
                                detail.getProduct().getId(),
                                detail.getCantidad(),
                                detail.getDiscountAmount()
                        ))
                        .toList()
        );

        SaleResponse savedSale = saleService.create(companyId, userId, saleRequest);
        Sale convertedSale = saleRepository.findById(savedSale.id())
                .orElseThrow(() -> new IllegalArgumentException("Venta convertida no encontrada."));

        budget.setEstado("CONVERTIDO");
        budget.setConvertedSale(convertedSale);
        budgetRepository.save(budget);

        return savedSale.id();
    }

    private BigDecimal resolveBudgetUnitPrice(Product product, BigDecimal quantity) {
        if (product.getPrecioVentaMayorista() != null
                && product.getCantidadMayoristaMinima() != null
                && quantity.compareTo(product.getCantidadMayoristaMinima()) >= 0) {
            return product.getPrecioVentaMayorista();
        }

        return product.getPrecioVenta();
    }

    private BigDecimal sanitizeDiscountAmount(BigDecimal discountAmount, BigDecimal maxAllowed) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        if (discountAmount.compareTo(maxAllowed) > 0) {
            return maxAllowed;
        }

        return discountAmount;
    }


}

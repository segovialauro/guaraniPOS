package com.guarani.pos.sale.service;

import static com.guarani.pos.sale.security.SalePermission.VENTA_CREAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_DESCARGAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_VER;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.cash.model.CashSession;
import com.guarani.pos.cash.repository.CashSessionRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.customer.model.Customer;
import com.guarani.pos.customer.repository.CustomerRepository;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import com.guarani.pos.sale.dto.SaleCreateRequest;
import com.guarani.pos.sale.dto.SaleDetailResponse;
import com.guarani.pos.sale.dto.SaleItemRequest;
import com.guarani.pos.sale.dto.SalePaymentRequest;
import com.guarani.pos.sale.dto.SalePaymentResponse;
import com.guarani.pos.sale.dto.SaleResponse;
import com.guarani.pos.sale.dto.SaleTicketDetailResponse;
import com.guarani.pos.sale.dto.SaleTicketResponse;
import com.guarani.pos.sale.model.Sale;
import com.guarani.pos.sale.model.SaleDetail;
import com.guarani.pos.sale.model.SalePayment;
import com.guarani.pos.sale.repository.SalePaymentRepository;
import com.guarani.pos.sale.repository.SaleRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class SaleService {

    private static final List<String> ALLOWED_METHODS = List.of(
            "EFECTIVO", "TRANSFERENCIA", "TARJETA_DEBITO", "TARJETA_CREDITO", "QR");

    private final SaleRepository saleRepository;
    private final SalePaymentRepository salePaymentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CashSessionRepository cashSessionRepository;
    private final AuthorizationService authorizationService;

    public SaleService(SaleRepository saleRepository,
                       SalePaymentRepository salePaymentRepository,
                       ProductRepository productRepository,
                       CustomerRepository customerRepository,
                       CompanyRepository companyRepository,
                       UserRepository userRepository,
                       CashSessionRepository cashSessionRepository,
                       AuthorizationService authorizationService) {
        this.saleRepository = saleRepository;
        this.salePaymentRepository = salePaymentRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.cashSessionRepository = cashSessionRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findRecent(Long companyId) {
        return saleRepository.findTop20ByCompanyIdOrderByFechaDesc(companyId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public SaleResponse create(Long companyId, Long userId, SaleCreateRequest request) {
        authorizationService.checkPermission(userId, VENTA_CREAR);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        CashSession cashSession = cashSessionRepository.findFirstByCompany_IdAndEstadoOrderByOpenedAtDesc(companyId, "ABIERTA")
                .orElseThrow(() -> new IllegalArgumentException("Debe abrir una caja antes de registrar ventas."));

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findByIdAndCompanyId(request.customerId(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
        }

        Sale sale = new Sale();
        sale.setCompany(company);
        sale.setCustomer(customer);
        sale.setCreatedBy(user);
        sale.setEstado("CONFIRMADA");
        sale.setObservacion(request.observation());
        sale.setNumeroOperacion(generateOperationNumber(companyId));
        sale.setFecha(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequest item : request.items()) {
            Product product = productRepository.findByIdAndCompanyId(item.productId(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productId()));

            if (!product.isActivo()) {
                throw new IllegalArgumentException("El producto está inactivo: " + product.getNombre());
            }

            if (product.getStockActual().compareTo(item.quantity()) < 0) {
                throw new IllegalArgumentException("Stock insuficiente para: " + product.getNombre());
            }

            BigDecimal subtotal = product.getPrecioVenta().multiply(item.quantity());

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setProductoCodigo(product.getCodigo());
            detail.setProductoNombre(product.getNombre());
            detail.setCantidad(item.quantity());
            detail.setPrecioUnitario(product.getPrecioVenta());
            detail.setSubtotal(subtotal);
            sale.addDetail(detail);

            product.setStockActual(product.getStockActual().subtract(item.quantity()));
            total = total.add(subtotal);
        }

        List<SalePaymentRequest> paymentRequests = normalizePayments(request, total);
        BigDecimal paymentTotal = paymentRequests.stream().map(SalePaymentRequest::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (paymentTotal.compareTo(total) != 0) {
            throw new IllegalArgumentException("La suma de cobros debe coincidir exactamente con el total de la venta.");
        }

        BigDecimal cashAmount = getPaymentAmount(paymentRequests, "EFECTIVO");
        BigDecimal amountReceived = cashAmount.compareTo(BigDecimal.ZERO) > 0 ? nvl(request.amountReceived()) : null;
        BigDecimal changeDue = BigDecimal.ZERO;
        if (cashAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (amountReceived == null || amountReceived.compareTo(cashAmount) < 0) {
                throw new IllegalArgumentException("El monto recibido en efectivo debe ser mayor o igual al total cobrado en efectivo.");
            }
            changeDue = amountReceived.subtract(cashAmount);
        }

        sale.setMetodoPago(resolveSalePaymentMethod(paymentRequests, request.paymentMethod()));
        sale.setMontoRecibido(amountReceived);
        sale.setVuelto(changeDue);
        sale.setTotal(total);

        for (SalePaymentRequest paymentRequest : paymentRequests) {
            SalePayment payment = new SalePayment();
            payment.setCashSession(cashSession);
            payment.setMethod(normalizeMethod(paymentRequest.method()));
            payment.setAmount(paymentRequest.amount());
            payment.setReference(paymentRequest.reference());
            sale.addPayment(payment);
        }

        updateCashSessionTotals(cashSession, paymentRequests, total);

        Sale saved = saleRepository.save(sale);
        salePaymentRepository.flush();
        return toResponse(saved);
    }

    private List<SalePaymentRequest> normalizePayments(SaleCreateRequest request, BigDecimal total) {
        List<SalePaymentRequest> result = new ArrayList<>();
        if (request.payments() != null && !request.payments().isEmpty()) {
            result.addAll(request.payments().stream()
                    .filter(p -> p.amount() != null && p.amount().compareTo(BigDecimal.ZERO) > 0)
                    .map(p -> new SalePaymentRequest(normalizeMethod(p.method()), p.amount(), trimToNull(p.reference())))
                    .toList());
        }

        if (result.isEmpty()) {
            result.add(new SalePaymentRequest(normalizeMethod(request.paymentMethod()), total, null));
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Debe informar al menos una forma de cobro.");
        }
        return result;
    }

    private void updateCashSessionTotals(CashSession cashSession, List<SalePaymentRequest> paymentRequests, BigDecimal total) {
        for (SalePaymentRequest payment : paymentRequests) {
            BigDecimal amount = nvl(payment.amount());
            switch (payment.method()) {
                case "EFECTIVO" -> cashSession.setCashSystem(nvl(cashSession.getCashSystem()).add(amount));
                case "TRANSFERENCIA" -> cashSession.setTransferSystem(nvl(cashSession.getTransferSystem()).add(amount));
                case "TARJETA_DEBITO" -> cashSession.setDebitCardSystem(nvl(cashSession.getDebitCardSystem()).add(amount));
                case "TARJETA_CREDITO" -> cashSession.setCreditCardSystem(nvl(cashSession.getCreditCardSystem()).add(amount));
                case "QR" -> cashSession.setQrSystem(nvl(cashSession.getQrSystem()).add(amount));
                default -> throw new IllegalArgumentException("Método de pago no soportado: " + payment.method());
            }
        }
        cashSession.setTotalSystem(nvl(cashSession.getTotalSystem()).add(total));
    }

    private BigDecimal getPaymentAmount(List<SalePaymentRequest> payments, String method) {
        return payments.stream()
                .filter(p -> method.equals(p.method()))
                .map(SalePaymentRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String resolveSalePaymentMethod(List<SalePaymentRequest> paymentRequests, String fallbackMethod) {
        return paymentRequests.size() > 1 ? "MIXTO" : normalizeMethod(fallbackMethod != null && !fallbackMethod.isBlank()
                ? fallbackMethod
                : paymentRequests.get(0).method());
    }

    private String normalizeMethod(String method) {
        String normalized = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw new IllegalArgumentException("Método de pago inválido: " + method);
        }
        return normalized;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String generateOperationNumber(Long companyId) {
        long next = saleRepository.countByCompanyId(companyId) + 1;
        String period = YearMonth.now().toString().replace("-", "");
        return "V-" + period + "-" + new DecimalFormat("000000").format(next);
    }

    private SaleResponse toResponse(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getNumeroOperacion(),
                sale.getFecha(),
                sale.getCustomer() != null ? sale.getCustomer().getId() : null,
                sale.getCustomer() != null ? sale.getCustomer().getNombre() : null,
                sale.getMetodoPago(),
                sale.getEstado(),
                sale.getTotal(),
                sale.getMontoRecibido(),
                sale.getVuelto(),
                sale.getPayments().stream()
                        .map(p -> new SalePaymentResponse(p.getMethod(), p.getAmount(), p.getReference()))
                        .toList(),
                sale.getDetails().stream()
                        .map(d -> new SaleDetailResponse(d.getProduct().getId(), d.getProductoCodigo(),
                                d.getProductoNombre(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                        .toList());
    }

    @Transactional(readOnly = true)
    public SaleTicketResponse getTicket(Long companyId, Long userId, Long saleId) {
        authorizationService.checkPermission(userId, VENTA_TICKET_VER);

        Sale sale = saleRepository.findByIdAndCompany_Id(saleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada."));

        String companyName = sale.getCompany() != null ? sale.getCompany().getName() : "-";
        String companyRuc = sale.getCompany() != null ? sale.getCompany().getRuc() : "-";
        String customerName = sale.getCustomer() != null ? sale.getCustomer().getNombre() : "Cliente contado";
        String cashierName = sale.getCreatedBy() != null ? sale.getCreatedBy().getFullName() : "-";

        return new SaleTicketResponse(
                sale.getId(),
                sale.getNumeroOperacion(),
                companyName,
                companyRuc,
                customerName,
                cashierName,
                sale.getFecha(),
                sale.getMetodoPago(),
                sale.getEstado(),
                sale.getObservacion(),
                sale.getTotal(),
                sale.getMontoRecibido(),
                sale.getVuelto(),
                sale.getPayments().stream()
                        .map(p -> new SalePaymentResponse(p.getMethod(), p.getAmount(), p.getReference()))
                        .toList(),
                sale.getDetails().stream()
                        .map(d -> new SaleTicketDetailResponse(
                                d.getProductoCodigo(),
                                d.getProductoNombre(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getSubtotal()))
                        .toList());
    }

    @Transactional(readOnly = true)
    public byte[] generateTicketPdf(Long companyId, Long userId, Long saleId) {
        authorizationService.checkPermission(userId, VENTA_TICKET_DESCARGAR);

        SaleTicketResponse ticket = getTicket(companyId, userId, saleId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A6, 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD);

            Paragraph company = new Paragraph(safe(ticket.companyName()), titleFont);
            company.setAlignment(Paragraph.ALIGN_CENTER);
            company.setSpacingAfter(4f);
            document.add(company);

            Paragraph ruc = new Paragraph("RUC: " + safe(ticket.companyRuc()), normalFont);
            ruc.setAlignment(Paragraph.ALIGN_CENTER);
            ruc.setSpacingAfter(8f);
            document.add(ruc);

            Paragraph op = new Paragraph("Ticket: " + safe(ticket.operationNumber()), boldFont);
            op.setSpacingAfter(4f);
            document.add(op);

            document.add(new Paragraph("Fecha: " + formatDateTime(ticket.date()), normalFont));
            document.add(new Paragraph("Cajero: " + safe(ticket.cashierName()), normalFont));
            document.add(new Paragraph("Cliente: " + safe(ticket.customerName()), normalFont));
            document.add(new Paragraph("Pago: " + safe(ticket.paymentMethod()), normalFont));
            document.add(new Paragraph("Estado: " + safe(ticket.status()), normalFont));
            document.add(new Paragraph(" ", normalFont));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 2f});

            for (SaleTicketDetailResponse item : ticket.items()) {
                String left = item.quantity().stripTrailingZeros().toPlainString() + " x " + safe(item.productName());
                table.addCell(left);
                table.addCell(formatMoney(item.subtotal()));
            }
            document.add(table);

            document.add(new Paragraph(" ", normalFont));
            for (SalePaymentResponse payment : ticket.payments()) {
                document.add(new Paragraph("Cobro " + labelForMethod(payment.method()) + ": " + formatMoney(payment.amount()), normalFont));
                if (payment.reference() != null && !payment.reference().isBlank()) {
                    document.add(new Paragraph("Ref: " + payment.reference(), normalFont));
                }
            }
            if (ticket.amountReceived() != null) {
                document.add(new Paragraph("Recibido: " + formatMoney(ticket.amountReceived()), normalFont));
            }
            if (ticket.changeDue() != null && ticket.changeDue().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Vuelto: " + formatMoney(ticket.changeDue()), normalFont));
            }

            Paragraph total = new Paragraph("TOTAL: " + formatMoney(ticket.total()), boldFont);
            total.setAlignment(Paragraph.ALIGN_RIGHT);
            total.setSpacingBefore(8f);
            document.add(total);

            if (ticket.observation() != null && !ticket.observation().isBlank()) {
                document.add(new Paragraph(" ", normalFont));
                document.add(new Paragraph("Obs: " + ticket.observation(), normalFont));
            }

            Paragraph footer = new Paragraph("Gracias por su compra", normalFont);
            footer.setAlignment(Paragraph.ALIGN_CENTER);
            footer.setSpacingBefore(12f);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el ticket PDF.", e);
        }
    }

    private String safe(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("es", "PY"));
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
        return "Gs. " + formatter.format(value != null ? value : BigDecimal.ZERO);
    }

    private String labelForMethod(String method) {
        return switch (method) {
            case "EFECTIVO" -> "efectivo";
            case "TRANSFERENCIA" -> "transferencia";
            case "TARJETA_DEBITO" -> "tarjeta débito";
            case "TARJETA_CREDITO" -> "tarjeta crédito";
            case "QR" -> "QR";
            default -> method;
        };
    }
}

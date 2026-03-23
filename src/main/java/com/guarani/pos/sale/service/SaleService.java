package com.guarani.pos.sale.service;

import static com.guarani.pos.sale.security.SalePermission.VENTA_CREAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_ANULAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_DEVOLVER;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_DESCARGAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_VER;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.billing.model.BillingConfig;
import com.guarani.pos.billing.repository.BillingConfigRepository;
import com.guarani.pos.cash.model.CashSession;
import com.guarani.pos.cash.repository.CashSessionRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.customer.model.Customer;
import com.guarani.pos.customer.repository.CustomerRepository;
import com.guarani.pos.inventory.model.InventoryMovement;
import com.guarani.pos.inventory.repository.InventoryMovementRepository;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import com.guarani.pos.sale.dto.SaleCreateRequest;
import com.guarani.pos.sale.dto.SaleCancelRequest;
import com.guarani.pos.sale.dto.SaleReturnItemRequest;
import com.guarani.pos.sale.dto.SaleReturnRequest;
import com.guarani.pos.sale.dto.SaleDetailResponse;
import com.guarani.pos.sale.dto.SaleItemRequest;
import com.guarani.pos.sale.dto.SalePaymentRequest;
import com.guarani.pos.sale.dto.SalePaymentResponse;
import com.guarani.pos.sale.dto.SaleResponse;
import com.guarani.pos.sale.dto.SaleTicketDetailResponse;
import com.guarani.pos.sale.dto.SaleTicketResponse;
import com.guarani.pos.sale.dto.SaleVatSummaryResponse;
import com.guarani.pos.sale.dto.SalesOperationalCashierSummaryResponse;
import com.guarani.pos.sale.dto.SalesOperationalSummaryResponse;
import com.guarani.pos.sale.model.Sale;
import com.guarani.pos.sale.model.SaleDetail;
import com.guarani.pos.sale.model.SalePayment;
import com.guarani.pos.sale.repository.SalePaymentRepository;
import com.guarani.pos.sale.repository.SaleRepository;
import com.guarani.pos.security.SecurityUtils;
import com.guarani.pos.subscription.service.SubscriptionAccessService;
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
    private final BillingConfigRepository billingConfigRepository;
    private final SubscriptionAccessService subscriptionAccessService;
    private final InventoryMovementRepository inventoryMovementRepository;

    public SaleService(SaleRepository saleRepository,
                       SalePaymentRepository salePaymentRepository,
                       ProductRepository productRepository,
                       CustomerRepository customerRepository,
                       CompanyRepository companyRepository,
                       UserRepository userRepository,
                       CashSessionRepository cashSessionRepository,
                       AuthorizationService authorizationService,
                       BillingConfigRepository billingConfigRepository,
                       SubscriptionAccessService subscriptionAccessService,
                       InventoryMovementRepository inventoryMovementRepository) {
        this.saleRepository = saleRepository;
        this.salePaymentRepository = salePaymentRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.cashSessionRepository = cashSessionRepository;
        this.authorizationService = authorizationService;
        this.billingConfigRepository = billingConfigRepository;
        this.subscriptionAccessService = subscriptionAccessService;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findRecent(Long companyId) {
        return saleRepository.findTop20ByCompanyIdOrderByFechaDesc(companyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findAuditHistory(Long companyId, String from, String to, String status, String query) {
        LocalDateTime fromDate = from != null && !from.isBlank()
                ? LocalDate.parse(from).atStartOfDay()
                : LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime toDate = to != null && !to.isBlank()
                ? LocalDate.parse(to).plusDays(1).atStartOfDay()
                : LocalDate.of(2100, 1, 1).atStartOfDay();
        String normalizedStatus = status != null && !status.isBlank() ? status.trim().toUpperCase(Locale.ROOT) : "";
        String queryPattern = query != null && !query.isBlank()
                ? "%" + query.trim().toUpperCase(Locale.ROOT) + "%"
                : "";

        return saleRepository.findAuditHistory(
                        companyId,
                        fromDate,
                        toDate,
                        normalizedStatus,
                        queryPattern,
                        PageRequest.of(0, 200))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesOperationalSummaryResponse getOperationalSummary(Long companyId, String date) {
        LocalDate summaryDate = date != null && !date.isBlank()
                ? LocalDate.parse(date)
                : LocalDate.now();
        LocalDateTime fromDate = summaryDate.atStartOfDay();
        LocalDateTime toDate = summaryDate.plusDays(1).atStartOfDay();

        List<SaleResponse> sales = saleRepository.findAuditHistory(
                        companyId,
                        fromDate,
                        toDate,
                        "",
                        "",
                        PageRequest.of(0, 500))
                .stream()
                .map(this::toResponse)
                .toList();

        List<SalesOperationalCashierSummaryResponse> cashiers =
                saleRepository.summarizeOperationalByCashier(companyId, fromDate, toDate);

        return new SalesOperationalSummaryResponse(
                summaryDate.toString(),
                sales.stream().filter(s -> "CONFIRMADA".equals(s.status())).count(),
                sales.stream()
                        .filter(s -> "CONFIRMADA".equals(s.status()))
                        .map(SaleResponse::total)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                sales.stream().filter(s -> "ANULADA".equals(s.status())).count(),
                sales.stream()
                        .filter(s -> "ANULADA".equals(s.status()))
                        .map(SaleResponse::subtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                sales.stream().filter(s -> "DEVUELTA_PARCIAL".equals(s.status())).count(),
                sales.stream()
                        .filter(s -> "DEVUELTA_PARCIAL".equals(s.status()))
                        .map(SaleResponse::returnTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                sales.stream().filter(s -> "DEVUELTA_TOTAL".equals(s.status())).count(),
                sales.stream()
                        .filter(s -> "DEVUELTA_TOTAL".equals(s.status()))
                        .map(SaleResponse::returnTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                cashiers);
    }

    @Transactional
    public SaleResponse create(Long companyId, Long userId, SaleCreateRequest request) {
        authorizationService.checkPermission(userId, VENTA_CREAR);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        CashSession cashSession = cashSessionRepository
                .findFirstByCompany_IdAndUser_IdAndEstadoOrderByOpenedAtDesc(companyId, userId, "ABIERTA")
                .orElseThrow(() -> new IllegalArgumentException("Debe abrir una caja antes de registrar ventas para el usuario actual."));

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

        BigDecimal subtotalBeforeDiscounts = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;

        for (SaleItemRequest item : request.items()) {
            Product product = productRepository.findByIdAndCompanyId(item.productId(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productId()));

            if (!product.isActivo()) {
                throw new IllegalArgumentException("El producto está inactivo: " + product.getNombre());
            }

            if (product.getStockActual().compareTo(item.quantity()) < 0) {
                throw new IllegalArgumentException("Stock insuficiente para: " + product.getNombre());
            }

            BigDecimal grossSubtotal = product.getPrecioVenta().multiply(item.quantity());
            BigDecimal itemDiscount = normalizeDiscount(item.discountAmount(), grossSubtotal);
            BigDecimal subtotal = grossSubtotal.subtract(itemDiscount);

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setProductoCodigo(product.getCodigo());
            detail.setProductoNombre(product.getNombre());
            detail.setCantidad(item.quantity());
            detail.setPrecioUnitario(product.getPrecioVenta());
            detail.setGrossSubtotal(grossSubtotal);
            detail.setDiscountAmount(itemDiscount);
            detail.setSubtotal(subtotal);
            detail.setVatType(normalizeVatType(product.getVatType()));
            sale.addDetail(detail);

            BigDecimal previousStock = nvl(product.getStockActual());
            BigDecimal newStock = previousStock.subtract(item.quantity());
            product.setStockActual(newStock);
            registerInventoryMovement(
                    company,
                    product,
                    user,
                    "VENTA",
                    item.quantity(),
                    previousStock,
                    newStock,
                    "Salida por venta " + sale.getNumeroOperacion());
            subtotalBeforeDiscounts = subtotalBeforeDiscounts.add(grossSubtotal);
            discountTotal = discountTotal.add(itemDiscount);
            total = total.add(subtotal);
        }

        BigDecimal globalDiscount = normalizeDiscount(request.globalDiscountAmount(), total);
        if (globalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            allocateGlobalDiscount(sale.getDetails(), globalDiscount, total);
            discountTotal = discountTotal.add(globalDiscount);
            total = total.subtract(globalDiscount);
        }

        validateDiscountLimit(subtotalBeforeDiscounts, discountTotal, SecurityUtils.getCurrentRole());

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
        sale.setSubtotal(subtotalBeforeDiscounts);
        sale.setDescuentoTotal(discountTotal);
        sale.setTotal(total);

        assignFiscalDataIfRequired(companyId, sale);

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

    @Transactional
    public SaleResponse cancel(Long companyId, Long userId, Long saleId, SaleCancelRequest request) {
        authorizationService.checkPermission(userId, VENTA_ANULAR);

        Sale sale = saleRepository.findByIdAndCompany_Id(saleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada."));

        if (!"CONFIRMADA".equalsIgnoreCase(sale.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden anular ventas confirmadas.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        for (SaleDetail detail : sale.getDetails()) {
            Product product = detail.getProduct();
            BigDecimal previousStock = nvl(product.getStockActual());
            BigDecimal quantity = nvl(detail.getCantidad());
            BigDecimal newStock = previousStock.add(quantity);
            product.setStockActual(newStock);
            registerInventoryMovement(
                    sale.getCompany(),
                    product,
                    user,
                    "ANULACION",
                    quantity,
                    previousStock,
                    newStock,
                    "Reposicion por anulacion de venta " + sale.getNumeroOperacion());
        }

        for (SalePayment payment : sale.getPayments()) {
            if (payment.getCashSession() == null) {
                continue;
            }

            revertCashSessionTotals(payment.getCashSession(), payment);
        }

        sale.setEstado("ANULADA");
        sale.setCancellationReason(trimToNull(request.reason()));
        sale.setCanceledAt(LocalDateTime.now());
        sale.setCanceledBy(user);

        return toResponse(sale);
    }

    @Transactional
    public SaleResponse partialReturn(Long companyId, Long userId, Long saleId, SaleReturnRequest request) {
        authorizationService.checkPermission(userId, VENTA_DEVOLVER);

        Sale sale = saleRepository.findByIdAndCompany_Id(saleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada."));

        if (!"CONFIRMADA".equalsIgnoreCase(sale.getEstado()) && !"DEVUELTA_PARCIAL".equalsIgnoreCase(sale.getEstado())) {
            throw new IllegalArgumentException("Solo se permiten devoluciones sobre ventas confirmadas o parcialmente devueltas.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        BigDecimal returnAmount = BigDecimal.ZERO;

        for (SaleReturnItemRequest item : request.items()) {
            SaleDetail detail = sale.getDetails().stream()
                    .filter(d -> d.getProduct() != null && item.productId().equals(d.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la venta: " + item.productId()));

            BigDecimal availableQuantity = nvl(detail.getCantidad()).subtract(nvl(detail.getReturnedQuantity()));
            if (item.quantity().compareTo(availableQuantity) > 0) {
                throw new IllegalArgumentException("La cantidad a devolver supera lo disponible para: " + detail.getProductoNombre());
            }

            BigDecimal lineNetUnit = nvl(detail.getSubtotal()).divide(nvl(detail.getCantidad()), 4, RoundingMode.HALF_UP);
            BigDecimal itemReturnAmount = lineNetUnit.multiply(item.quantity()).setScale(2, RoundingMode.HALF_UP);

            detail.setReturnedQuantity(nvl(detail.getReturnedQuantity()).add(item.quantity()));
            detail.setReturnedAmount(nvl(detail.getReturnedAmount()).add(itemReturnAmount));
            detail.setSubtotal(nvl(detail.getSubtotal()).subtract(itemReturnAmount));

            Product product = detail.getProduct();
            BigDecimal previousStock = nvl(product.getStockActual());
            BigDecimal newStock = previousStock.add(item.quantity());
            product.setStockActual(newStock);
            registerInventoryMovement(
                    sale.getCompany(),
                    product,
                    user,
                    "DEVOLUCION",
                    item.quantity(),
                    previousStock,
                    newStock,
                    "Reposicion por devolucion de venta " + sale.getNumeroOperacion());

            returnAmount = returnAmount.add(itemReturnAmount);
        }

        if (returnAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La devolucion no genero ningun monto a revertir.");
        }

        revertSalePaymentsProportionally(sale, returnAmount);

        sale.setReturnReason(trimToNull(request.reason()));
        sale.setReturnedAt(LocalDateTime.now());
        sale.setReturnedBy(user);
        sale.setReturnTotal(nvl(sale.getReturnTotal()).add(returnAmount));
        sale.setTotal(nvl(sale.getTotal()).subtract(returnAmount));
        sale.setEstado(hasPendingReturnableItems(sale) ? "DEVUELTA_PARCIAL" : "DEVUELTA_TOTAL");

        return toResponse(sale);
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

    private void revertCashSessionTotals(CashSession cashSession, SalePayment payment) {
        BigDecimal amount = nvl(payment.getAmount());
        switch (payment.getMethod()) {
            case "EFECTIVO" -> cashSession.setCashSystem(nonNegativeSubtract(cashSession.getCashSystem(), amount));
            case "TRANSFERENCIA" -> cashSession.setTransferSystem(nonNegativeSubtract(cashSession.getTransferSystem(), amount));
            case "TARJETA_DEBITO" -> cashSession.setDebitCardSystem(nonNegativeSubtract(cashSession.getDebitCardSystem(), amount));
            case "TARJETA_CREDITO" -> cashSession.setCreditCardSystem(nonNegativeSubtract(cashSession.getCreditCardSystem(), amount));
            case "QR" -> cashSession.setQrSystem(nonNegativeSubtract(cashSession.getQrSystem(), amount));
            default -> throw new IllegalArgumentException("Metodo de pago no soportado: " + payment.getMethod());
        }
        cashSession.setTotalSystem(nonNegativeSubtract(cashSession.getTotalSystem(), amount));
    }

    private void revertSalePaymentsProportionally(Sale sale, BigDecimal returnAmount) {
        List<SalePayment> payments = sale.getPayments();
        BigDecimal originalTotal = nvl(sale.getTotal());
        BigDecimal remaining = returnAmount;

        for (int i = 0; i < payments.size(); i++) {
            SalePayment payment = payments.get(i);
            if (payment.getCashSession() == null) {
                continue;
            }

            BigDecimal refundAmount;
            if (i == payments.size() - 1) {
                refundAmount = remaining;
            } else {
                refundAmount = nvl(payment.getAmount())
                        .multiply(returnAmount)
                        .divide(originalTotal, 2, RoundingMode.HALF_UP);
            }

            SalePayment refundPayment = new SalePayment();
            refundPayment.setCashSession(payment.getCashSession());
            refundPayment.setMethod(payment.getMethod());
            refundPayment.setAmount(refundAmount);
            revertCashSessionTotals(payment.getCashSession(), refundPayment);
            remaining = remaining.subtract(refundAmount);
        }
    }

    private boolean hasPendingReturnableItems(Sale sale) {
        return sale.getDetails().stream()
                .anyMatch(detail -> nvl(detail.getCantidad()).compareTo(nvl(detail.getReturnedQuantity())) > 0);
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

    private BigDecimal nonNegativeSubtract(BigDecimal current, BigDecimal amount) {
        BigDecimal result = nvl(current).subtract(nvl(amount));
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    private BigDecimal normalizeDiscount(BigDecimal requestedDiscount, BigDecimal maxAllowed) {
        BigDecimal safeDiscount = nvl(requestedDiscount);
        BigDecimal safeMaxAllowed = nvl(maxAllowed);

        if (safeDiscount.compareTo(BigDecimal.ZERO) <= 0 || safeMaxAllowed.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return safeDiscount.compareTo(safeMaxAllowed) > 0 ? safeMaxAllowed : safeDiscount;
    }

    private void allocateGlobalDiscount(List<SaleDetail> details, BigDecimal globalDiscount, BigDecimal netSubtotal) {
        if (details == null || details.isEmpty() || globalDiscount == null || globalDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal remainingDiscount = globalDiscount;

        for (int i = 0; i < details.size(); i++) {
            SaleDetail detail = details.get(i);
            BigDecimal currentSubtotal = nvl(detail.getSubtotal());
            BigDecimal allocatedDiscount;

            if (i == details.size() - 1) {
                allocatedDiscount = normalizeDiscount(remainingDiscount, currentSubtotal);
            } else {
                allocatedDiscount = globalDiscount
                        .multiply(currentSubtotal)
                        .divide(netSubtotal, 2, RoundingMode.HALF_UP);
                allocatedDiscount = normalizeDiscount(allocatedDiscount, currentSubtotal);
            }

            detail.setDiscountAmount(nvl(detail.getDiscountAmount()).add(allocatedDiscount));
            detail.setSubtotal(currentSubtotal.subtract(allocatedDiscount));
            remainingDiscount = remainingDiscount.subtract(allocatedDiscount);
        }
    }

    private void validateDiscountLimit(BigDecimal subtotalBeforeDiscounts, BigDecimal discountTotal, String roleCode) {
        BigDecimal maxAllowedDiscount = calculateMaxAllowedDiscount(subtotalBeforeDiscounts, roleCode);

        if (discountTotal.compareTo(maxAllowedDiscount) > 0) {
            throw new IllegalArgumentException("El descuento supera el limite permitido para el rol actual.");
        }
    }

    private BigDecimal calculateMaxAllowedDiscount(BigDecimal subtotalBeforeDiscounts, String roleCode) {
        BigDecimal subtotal = nvl(subtotalBeforeDiscounts);
        String normalizedRole = normalizeRole(roleCode);

        return switch (normalizedRole) {
            case "CAJERO" -> subtotal.multiply(BigDecimal.valueOf(0.10)).setScale(0, RoundingMode.DOWN);
            case "SUPERVISOR" -> subtotal.multiply(BigDecimal.valueOf(0.25)).setScale(0, RoundingMode.DOWN);
            default -> subtotal;
        };
    }

    private String normalizeRole(String roleCode) {
        String normalized = roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ADMIN")) {
            return "ADMIN_EMPRESA";
        }
        if (normalized.startsWith("SUPERVISOR")) {
            return "SUPERVISOR";
        }
        if (normalized.startsWith("CAJERO")) {
            return "CAJERO";
        }

        return normalized;
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
                sale.getFiscalDocumentType(),
                sale.getFiscalInvoiceNumber(),
                sale.getFiscalTimbradoNumber(),
                sale.getFiscalEstablishmentCode(),
                sale.getFiscalExpeditionPoint(),
                sale.getEstado(),
                sale.getCancellationReason(),
                sale.getCanceledAt(),
                sale.getCanceledBy() != null ? sale.getCanceledBy().getFullName() : null,
                sale.getReturnReason(),
                sale.getReturnedAt(),
                sale.getReturnedBy() != null ? sale.getReturnedBy().getFullName() : null,
                sale.getSubtotal(),
                sale.getDescuentoTotal(),
                sale.getReturnTotal(),
                sale.getTotal(),
                sale.getMontoRecibido(),
                sale.getVuelto(),
                sale.getPayments().stream()
                        .map(p -> new SalePaymentResponse(p.getMethod(), p.getAmount(), p.getReference()))
                        .toList(),
                sale.getDetails().stream()
                        .map(d -> new SaleDetailResponse(d.getProduct().getId(), d.getProductoCodigo(),
                                d.getProductoNombre(), d.getCantidad(), d.getPrecioUnitario(),
                                d.getGrossSubtotal(), d.getDiscountAmount(),
                                d.getReturnedQuantity(), d.getReturnedAmount(), d.getSubtotal(),
                                normalizeVatType(d.getVatType())))
                        .toList(),
                calculateVatSummary(sale.getDetails()));
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
                sale.getFiscalDocumentType(),
                sale.getFiscalInvoiceNumber(),
                sale.getFiscalTimbradoNumber(),
                sale.getFiscalEstablishmentCode(),
                sale.getFiscalExpeditionPoint(),
                sale.getEstado(),
                sale.getObservacion(),
                sale.getSubtotal(),
                sale.getDescuentoTotal(),
                sale.getReturnTotal(),
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
                                d.getGrossSubtotal(),
                                d.getDiscountAmount(),
                                d.getReturnedQuantity(),
                                d.getReturnedAmount(),
                                d.getSubtotal(),
                                normalizeVatType(d.getVatType())))
                        .toList(),
                calculateVatSummary(sale.getDetails()));
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

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.4f, 1.6f, 2f});

            table.addCell("Producto");
            table.addCell("Cant.");
            table.addCell("IVA");
            table.addCell("Total");

            for (SaleTicketDetailResponse item : ticket.items()) {
                table.addCell(item.discountAmount() != null && item.discountAmount().compareTo(BigDecimal.ZERO) > 0
                        ? safe(item.productName()) + " (-" + formatMoney(item.discountAmount()) + ")"
                        : safe(item.productName()));
                table.addCell(item.quantity().stripTrailingZeros().toPlainString());
                table.addCell(labelForVatType(item.vatType()));
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

            if (ticket.discountTotal() != null && ticket.discountTotal().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Subtotal: " + formatMoney(ticket.subtotal()), normalFont));
                document.add(new Paragraph("Descuentos: -" + formatMoney(ticket.discountTotal()), normalFont));
            }

            Paragraph total = new Paragraph("TOTAL: " + formatMoney(ticket.total()), boldFont);
            total.setAlignment(Paragraph.ALIGN_RIGHT);
            total.setSpacingBefore(8f);
            document.add(total);

            if (ticket.vatSummary() != null) {
                document.add(new Paragraph(" ", normalFont));
                document.add(new Paragraph("Liquidacion IVA", boldFont));
                document.add(new Paragraph("Gravada 10%: " + formatMoney(ticket.vatSummary().taxableVat10()), normalFont));
                document.add(new Paragraph("Gravada 5%: " + formatMoney(ticket.vatSummary().taxableVat5()), normalFont));
                document.add(new Paragraph("Exentas: " + formatMoney(ticket.vatSummary().exemptTotal()), normalFont));
                document.add(new Paragraph("IVA 10%: " + formatMoney(ticket.vatSummary().vat10()), normalFont));
                document.add(new Paragraph("IVA 5%: " + formatMoney(ticket.vatSummary().vat5()), normalFont));
                document.add(new Paragraph("Total IVA: " + formatMoney(ticket.vatSummary().totalVat()), boldFont));
            }

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

    private SaleVatSummaryResponse calculateVatSummary(List<SaleDetail> details) {
        BigDecimal taxableVat10 = BigDecimal.ZERO;
        BigDecimal taxableVat5 = BigDecimal.ZERO;
        BigDecimal exemptTotal = BigDecimal.ZERO;
        BigDecimal vat10 = BigDecimal.ZERO;
        BigDecimal vat5 = BigDecimal.ZERO;

        for (SaleDetail detail : details) {
            BigDecimal subtotal = nvl(detail.getSubtotal());
            String vatType = normalizeVatType(detail.getVatType());

            switch (vatType) {
                case "IVA_10" -> {
                    taxableVat10 = taxableVat10.add(subtotal);
                    vat10 = vat10.add(subtotal.divide(BigDecimal.valueOf(11), 2, java.math.RoundingMode.HALF_UP));
                }
                case "IVA_5" -> {
                    taxableVat5 = taxableVat5.add(subtotal);
                    vat5 = vat5.add(subtotal.divide(BigDecimal.valueOf(21), 2, java.math.RoundingMode.HALF_UP));
                }
                case "EXENTO" -> exemptTotal = exemptTotal.add(subtotal);
                default -> {
                }
            }
        }

        return new SaleVatSummaryResponse(
                taxableVat10,
                taxableVat5,
                exemptTotal,
                vat10,
                vat5,
                vat10.add(vat5));
    }

    private String normalizeVatType(String vatType) {
        String normalized = vatType == null ? "IVA_10" : vatType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "IVA_10", "IVA_5", "EXENTO" -> normalized;
            default -> "IVA_10";
        };
    }

    private String labelForVatType(String vatType) {
        return switch (normalizeVatType(vatType)) {
            case "IVA_10" -> "10%";
            case "IVA_5" -> "5%";
            case "EXENTO" -> "EX";
            default -> "-";
        };
    }

    private void registerInventoryMovement(Company company,
                                           Product product,
                                           User user,
                                           String movementType,
                                           BigDecimal quantity,
                                           BigDecimal previousStock,
                                           BigDecimal newStock,
                                           String reason) {
        InventoryMovement movement = new InventoryMovement();
        movement.setCompany(company);
        movement.setProduct(product);
        movement.setUser(user);
        movement.setMovementType(movementType);
        movement.setQuantity(nvl(quantity));
        movement.setPreviousStock(nvl(previousStock));
        movement.setNewStock(nvl(newStock));
        movement.setReason(reason);
        inventoryMovementRepository.save(movement);
    }

    private void assignFiscalDataIfRequired(Long companyId, Sale sale) {
        BillingConfig config = billingConfigRepository.findFirstByCompany_IdOrderByIdDesc(companyId).orElse(null);
        if (config == null) {
            return;
        }

        String documentType = config.getDocumentType();
        if (!"FISCAL_PRINTER".equalsIgnoreCase(documentType) && !"ELECTRONICO".equalsIgnoreCase(documentType)) {
            return;
        }

        if ("FISCAL_PRINTER".equalsIgnoreCase(documentType)) {
            try {
                subscriptionAccessService.validateFiscalPrinterEnabled(companyId);
            } catch (IllegalArgumentException ex) {
                return;
            }
        }

        if ("ELECTRONICO".equalsIgnoreCase(documentType)) {
            try {
                subscriptionAccessService.validateElectronicInvoiceEnabled(companyId);
            } catch (IllegalArgumentException ex) {
                return;
            }
        }

        String nextInvoiceNumber = trimToNull(config.getInvoiceNumber());
        if (nextInvoiceNumber == null) {
            throw new IllegalArgumentException("Debe configurar el proximo numero de factura legal antes de vender.");
        }

        validateBillingConfigForSale(config);

        sale.setFiscalDocumentType(documentType);
        sale.setFiscalInvoiceNumber(nextInvoiceNumber);
        sale.setFiscalTimbradoNumber(trimToNull(config.getTimbradoNumber()));
        sale.setFiscalEstablishmentCode(trimToNull(config.getEstablishmentCode()));
        sale.setFiscalExpeditionPoint(trimToNull(config.getExpeditionPoint()));

        config.setInvoiceNumber(incrementFiscalInvoiceNumber(nextInvoiceNumber));
        billingConfigRepository.save(config);
    }

    private String incrementFiscalInvoiceNumber(String currentValue) {
        String trimmed = currentValue.trim();
        String digitsOnly = trimmed.replaceAll("\\D", "");
        if (digitsOnly.isEmpty()) {
            throw new IllegalArgumentException("El proximo numero de factura legal no es valido.");
        }

        java.math.BigInteger next = new java.math.BigInteger(digitsOnly).add(java.math.BigInteger.ONE);
        String incrementedDigits = next.toString();

        if (trimmed.matches("^\\d{3}-\\d{3}-\\d+$")) {
            String[] parts = trimmed.split("-");
            int sequentialLength = parts[2].length();
            String prefix = parts[0] + "-" + parts[1] + "-";
            String currentSequentialDigits = parts[2].replaceAll("\\D", "");
            java.math.BigInteger nextSequential = new java.math.BigInteger(currentSequentialDigits).add(java.math.BigInteger.ONE);
            return prefix + leftPad(nextSequential.toString(), sequentialLength);
        }

        return leftPad(incrementedDigits, digitsOnly.length());
    }

    private String leftPad(String value, int length) {
        if (value.length() >= length) {
            return value;
        }
        return "0".repeat(length - value.length()) + value;
    }

    private void validateBillingConfigForSale(BillingConfig config) {
        String establishmentCode = trimToNull(config.getEstablishmentCode());
        String expeditionPoint = trimToNull(config.getExpeditionPoint());
        String timbradoNumber = trimToNull(config.getTimbradoNumber());
        String timbradoValidity = trimToNull(config.getTimbradoValidity());
        String invoiceNumber = trimToNull(config.getInvoiceNumber());

        if (establishmentCode == null || !establishmentCode.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("El establecimiento configurado debe tener 3 digitos.");
        }

        if (expeditionPoint == null || !expeditionPoint.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("El punto de expedicion configurado debe tener 3 digitos.");
        }

        if (timbradoNumber == null || !timbradoNumber.matches("^\\d{7,}$")) {
            throw new IllegalArgumentException("El timbrado configurado no es valido.");
        }

        if (invoiceNumber == null || !invoiceNumber.matches("^\\d{3}-\\d{3}-\\d{7}$")) {
            throw new IllegalArgumentException(
                    "El proximo numero de factura legal debe tener formato 001-001-0000001.");
        }

        if (timbradoValidity == null) {
            throw new IllegalArgumentException("Debe configurar la vigencia del timbrado.");
        }

        LocalDate validityDate = LocalDate.parse(timbradoValidity);
        if (validityDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("El timbrado configurado esta vencido.");
        }
    }
}

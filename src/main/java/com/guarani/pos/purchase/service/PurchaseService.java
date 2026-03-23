package com.guarani.pos.purchase.service;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.inventory.model.InventoryMovement;
import com.guarani.pos.inventory.repository.InventoryMovementRepository;
import com.guarani.pos.purchase.dto.*;
import com.guarani.pos.purchase.model.Purchase;
import com.guarani.pos.purchase.model.PurchaseDetail;
import com.guarani.pos.purchase.model.PurchasePayment;
import com.guarani.pos.purchase.repository.PurchaseRepository;
import com.guarani.pos.purchase.security.PurchasePermission;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import com.guarani.pos.supplier.model.Supplier;
import com.guarani.pos.supplier.repository.SupplierRepository;
import com.guarani.pos.subscription.service.SubscriptionAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class PurchaseService {

    private static final List<String> ALLOWED_PAYMENT_METHODS = List.of(
            "EFECTIVO", "TRANSFERENCIA", "TARJETA_DEBITO", "TARJETA_CREDITO", "QR", "BANCARD_QR");

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final AuthorizationService authorizationService;
    private final SubscriptionAccessService subscriptionAccessService;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           InventoryMovementRepository inventoryMovementRepository,
                           AuthorizationService authorizationService,
                           SubscriptionAccessService subscriptionAccessService) {
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.authorizationService = authorizationService;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Transactional(readOnly = true)
    public PurchaseSummaryResponse getSummary(Long companyId, Long userId) {
        authorizationService.checkPermission(userId, PurchasePermission.COMPRAS_VER);

        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

        BigDecimal monthlyTotal = nvl(purchaseRepository.sumTotalByDateRange(companyId, firstDay, lastDay));
        BigDecimal payableTotal = nvl(purchaseRepository.sumPayableBalance(companyId));
        long pendingCount = purchaseRepository.countByCompanyIdAndBalanceGreaterThan(companyId, BigDecimal.ZERO);

        List<Purchase> purchases = purchaseRepository.findTop50ByCompanyIdOrderByPurchaseDateDescCreatedAtDesc(companyId);
        BigDecimal pendingTotal = purchases.stream()
                .filter(p -> p.getBalance() != null && p.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(Purchase::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PurchaseSummaryResponse(monthlyTotal, pendingTotal, payableTotal, pendingCount);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> findAll(Long companyId, Long userId, String q, String status, String from, String to) {
        authorizationService.checkPermission(userId, PurchasePermission.COMPRAS_VER);

        String normalizedStatus = status != null && !status.isBlank()
                ? status.trim().toUpperCase(Locale.ROOT)
                : "";
        String queryPattern = q != null && !q.isBlank()
                ? "%" + q.trim().toUpperCase(Locale.ROOT) + "%"
                : "";
        LocalDate fromDate = from != null && !from.isBlank() ? LocalDate.parse(from) : null;
        LocalDate toDate = to != null && !to.isBlank() ? LocalDate.parse(to) : null;

        return purchaseRepository.search(companyId, normalizedStatus, fromDate, toDate, queryPattern)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PurchaseResponse create(Long companyId, Long userId, PurchaseCreateRequest request) {
        authorizationService.checkPermission(userId, PurchasePermission.COMPRAS_REGISTRAR);

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la compra.");
        }

        Supplier supplier = supplierRepository.findByIdAndCompanyId(request.supplierId(), companyId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado."));

        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        long currentMonthCount = purchaseRepository.countByCompanyIdAndCreatedAtBetween(
                companyId,
                firstDay.atStartOfDay(),
                firstDay.plusMonths(1).atStartOfDay());
        subscriptionAccessService.validateCanRegisterPurchase(companyId, currentMonthCount);

        if (!supplier.isActive()) {
            throw new IllegalArgumentException("El proveedor esta inactivo.");
        }

        validateDuplicateInvoice(companyId, supplier.getId(), request.invoiceNumber().trim(), null);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Purchase purchase = new Purchase();
        purchase.setCompany(supplier.getCompany());
        purchase.setSupplier(supplier);
        purchase.setCreatedBy(user);
        purchase.setPurchaseType(normalizePurchaseType(request.purchaseType()));
        purchase.setInvoiceNumber(request.invoiceNumber().trim());
        purchase.setPurchaseDate(request.purchaseDate());
        purchase.setDueDate(request.dueDate());
        purchase.setPaymentCondition(normalizePaymentCondition(request.paymentCondition()));
        purchase.setObservation(trimToNull(request.observation()));
        purchase.setReceiptStatus("DIRECTA".equalsIgnoreCase(purchase.getPurchaseType()) ? "RECIBIDA_TOTAL" : "PENDIENTE");

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseItemRequest item : request.items()) {
            Product product = productRepository.findByIdAndCompanyId(item.productId(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productId()));

            BigDecimal lineSubtotal = item.quantity().multiply(item.costPrice());

            PurchaseDetail detail = new PurchaseDetail();
            detail.setProduct(product);
            detail.setProductCode(product.getCodigo());
            detail.setProductName(product.getNombre());
            detail.setQuantity(item.quantity());
            detail.setCostPrice(item.costPrice());
            detail.setReceivedQuantity("DIRECTA".equalsIgnoreCase(purchase.getPurchaseType()) ? item.quantity() : BigDecimal.ZERO);
            detail.setSubtotal(lineSubtotal);
            purchase.addDetail(detail);

            if ("DIRECTA".equalsIgnoreCase(purchase.getPurchaseType())) {
                BigDecimal previousStock = nvl(product.getStockActual());
                BigDecimal newStock = previousStock.add(item.quantity());
                product.setStockActual(newStock);
                product.setPrecioCosto(item.costPrice());
                registerInventoryMovement(
                        supplier.getCompany().getId(),
                        product,
                        user,
                        "COMPRA",
                        item.quantity(),
                        previousStock,
                        newStock,
                        "Entrada por compra " + request.invoiceNumber().trim()
                );
            }

            subtotal = subtotal.add(lineSubtotal);
        }

        BigDecimal initialPayment = nvl(request.initialPayment());
        if (initialPayment.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("El pago inicial no puede superar el total de la compra.");
        }

        purchase.setSubtotal(subtotal);
        purchase.setTotal(subtotal);
        purchase.setPaidAmount(initialPayment);
        purchase.setBalance(subtotal.subtract(initialPayment));
        purchase.setStatus(resolvePurchaseStatus(subtotal, initialPayment));

        if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
            PurchasePayment payment = new PurchasePayment();
            payment.setUser(user);
            payment.setAmount(initialPayment);
            payment.setMethod(normalizeMethod(request.initialPaymentMethod()));
            payment.setReference(null);
            payment.setObservation("Pago inicial");
            purchase.addPayment(payment);
        }

        return toResponse(purchaseRepository.save(purchase));
    }

    @Transactional
    public PurchaseResponse update(Long companyId, Long userId, Long purchaseId, PurchaseUpdateRequest request) {
        authorizationService.checkPermission(userId, PurchasePermission.COMPRAS_EDITAR);

        Purchase purchase = purchaseRepository.findByIdAndCompanyId(purchaseId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada."));

        validateCanEditPurchase(purchase);

        Supplier supplier = supplierRepository.findByIdAndCompanyId(request.supplierId(), companyId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado."));

        if (!supplier.isActive()) {
            throw new IllegalArgumentException("El proveedor esta inactivo.");
        }

        validateDuplicateInvoice(companyId, supplier.getId(), request.invoiceNumber().trim(), purchase.getId());

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la compra.");
        }

        purchase.setSupplier(supplier);
        purchase.setInvoiceNumber(request.invoiceNumber().trim());
        purchase.setPurchaseDate(request.purchaseDate());
        purchase.setDueDate(request.dueDate());
        purchase.setPaymentCondition(normalizePaymentCondition(request.paymentCondition()));
        purchase.setObservation(trimToNull(request.observation()));

        purchase.getDetails().clear();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseItemRequest item : request.items()) {
            Product product = productRepository.findByIdAndCompanyId(item.productId(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productId()));

            BigDecimal lineSubtotal = item.quantity().multiply(item.costPrice());

            PurchaseDetail detail = new PurchaseDetail();
            detail.setProduct(product);
            detail.setProductCode(product.getCodigo());
            detail.setProductName(product.getNombre());
            detail.setQuantity(item.quantity());
            detail.setCostPrice(item.costPrice());
            detail.setReceivedQuantity(BigDecimal.ZERO);
            detail.setSubtotal(lineSubtotal);
            purchase.addDetail(detail);

            subtotal = subtotal.add(lineSubtotal);
        }

        purchase.setSubtotal(subtotal);
        purchase.setTotal(subtotal);
        purchase.setPaidAmount(BigDecimal.ZERO);
        purchase.setBalance(subtotal);
        purchase.setStatus("PENDIENTE");

        return toResponse(purchaseRepository.save(purchase));
    }

    @Transactional
    public PurchaseResponse receive(Long companyId, Long userId, Long purchaseId, PurchaseReceiveRequest request) {
        authorizationService.checkPermission(userId, PurchasePermission.COMPRAS_REGISTRAR);

        Purchase purchase = purchaseRepository.findByIdAndCompanyId(purchaseId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada."));

        if (!"ORDEN".equalsIgnoreCase(purchase.getPurchaseType())) {
            throw new IllegalArgumentException("Solo las ordenes de compra admiten recepcion parcial.");
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un producto a recepcionar.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        for (PurchaseReceiveItemRequest item : request.items()) {
            PurchaseDetail detail = purchase.getDetails().stream()
                    .filter(d -> d.getProduct() != null && item.productId().equals(d.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la orden."));

            BigDecimal pendingQuantity = nvl(detail.getQuantity()).subtract(nvl(detail.getReceivedQuantity()));
            if (item.quantity().compareTo(pendingQuantity) > 0) {
                throw new IllegalArgumentException("La recepcion supera la cantidad pendiente de " + detail.getProductName() + ".");
            }

            Product product = detail.getProduct();
            BigDecimal previousStock = nvl(product.getStockActual());
            BigDecimal newStock = previousStock.add(item.quantity());
            product.setStockActual(newStock);
            product.setPrecioCosto(detail.getCostPrice());
            detail.setReceivedQuantity(nvl(detail.getReceivedQuantity()).add(item.quantity()));

            registerInventoryMovement(
                    purchase.getCompany().getId(),
                    product,
                    user,
                    "COMPRA",
                    item.quantity(),
                    previousStock,
                    newStock,
                    "Recepcion de orden " + purchase.getInvoiceNumber()
            );
        }

        boolean fullyReceived = purchase.getDetails().stream()
                .allMatch(detail -> nvl(detail.getReceivedQuantity()).compareTo(nvl(detail.getQuantity())) >= 0);
        purchase.setReceiptStatus(fullyReceived ? "RECIBIDA_TOTAL" : "RECIBIDA_PARCIAL");

        if (request.observation() != null && !request.observation().isBlank()) {
            String previousObservation = trimToNull(purchase.getObservation());
            String note = "Recepcion: " + request.observation().trim();
            purchase.setObservation(previousObservation == null ? note : previousObservation + " | " + note);
        }

        return toResponse(purchaseRepository.save(purchase));
    }

    @Transactional
    public PurchaseResponse registerPayment(Long companyId, Long userId, Long purchaseId, PurchasePaymentRequest request) {
        authorizationService.checkPermission(userId, PurchasePermission.COMPRAS_PAGAR);

        Purchase purchase = purchaseRepository.findByIdAndCompanyId(purchaseId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        BigDecimal amount = nvl(request.amount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El pago debe ser mayor a cero.");
        }

        if (amount.compareTo(nvl(purchase.getBalance())) > 0) {
            throw new IllegalArgumentException("El pago supera el saldo pendiente.");
        }

        PurchasePayment payment = new PurchasePayment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setMethod(normalizeMethod(request.method()));
        payment.setReference(trimToNull(request.reference()));
        payment.setObservation(trimToNull(request.observation()));
        purchase.addPayment(payment);

        BigDecimal newPaidAmount = nvl(purchase.getPaidAmount()).add(amount);
        BigDecimal newBalance = nvl(purchase.getTotal()).subtract(newPaidAmount);

        purchase.setPaidAmount(newPaidAmount);
        purchase.setBalance(newBalance);
        purchase.setStatus(resolvePurchaseStatus(purchase.getTotal(), newPaidAmount));

        return toResponse(purchaseRepository.save(purchase));
    }

    @Transactional
    public PurchaseResponse cancel(Long companyId, Long userId, Long purchaseId, PurchaseCancelRequest request) {
        authorizationService.checkPermission(userId, PurchasePermission.COMPRAS_ANULAR);

        Purchase purchase = purchaseRepository.findByIdAndCompanyId(purchaseId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada."));

        if ("ANULADA".equalsIgnoreCase(purchase.getStatus())) {
            throw new IllegalArgumentException("La compra ya fue anulada.");
        }

        if (!purchase.getPayments().isEmpty()) {
            throw new IllegalArgumentException("No se puede anular una compra con pagos registrados.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        for (PurchaseDetail detail : purchase.getDetails()) {
            BigDecimal receivedQuantity = nvl(detail.getReceivedQuantity());
            if (receivedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Product product = detail.getProduct();
            BigDecimal previousStock = nvl(product.getStockActual());
            BigDecimal newStock = previousStock.subtract(receivedQuantity);
            if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("No se puede anular porque el stock actual de " + detail.getProductName() + " es insuficiente para revertir la recepcion.");
            }

            product.setStockActual(newStock);
            registerInventoryMovement(
                    purchase.getCompany().getId(),
                    product,
                    user,
                    "ANULACION",
                    receivedQuantity,
                    previousStock,
                    newStock,
                    "Anulacion de compra " + purchase.getInvoiceNumber()
            );
        }

        purchase.setStatus("ANULADA");
        purchase.setReceiptStatus("ANULADA");
        purchase.setCancelReason(request.reason().trim());
        purchase.setCanceledAt(LocalDateTime.now());
        purchase.setCanceledBy(user);
        purchase.setBalance(BigDecimal.ZERO);

        return toResponse(purchaseRepository.save(purchase));
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getPurchaseType(),
                purchase.getReceiptStatus(),
                purchase.getInvoiceNumber(),
                purchase.getPurchaseDate(),
                purchase.getDueDate(),
                purchase.getSupplier().getId(),
                purchase.getSupplier().getName(),
                purchase.getSupplier().getRuc(),
                purchase.getPaymentCondition(),
                purchase.getStatus(),
                purchase.getSubtotal(),
                purchase.getTotal(),
                purchase.getPaidAmount(),
                purchase.getBalance(),
                purchase.getCancelReason(),
                purchase.getCanceledAt(),
                purchase.getCanceledBy() != null ? purchase.getCanceledBy().getFullName() : null,
                purchase.getObservation(),
                purchase.getCreatedBy().getFullName(),
                purchase.getCreatedAt(),
                purchase.getDetails().stream()
                        .map(detail -> new PurchaseDetailResponse(
                                detail.getProduct().getId(),
                                detail.getProductCode(),
                                detail.getProductName(),
                                detail.getQuantity(),
                                detail.getReceivedQuantity(),
                                detail.getCostPrice(),
                                detail.getSubtotal()
                        ))
                        .toList(),
                purchase.getPayments().stream()
                        .map(payment -> new PurchasePaymentResponse(
                                payment.getId(),
                                payment.getAmount(),
                                payment.getMethod(),
                                payment.getReference(),
                                payment.getObservation(),
                                payment.getUser().getFullName(),
                                payment.getCreatedAt()
                        ))
                        .toList()
        );
    }

    private void registerInventoryMovement(Long companyId,
                                           Product product,
                                           User user,
                                           String movementType,
                                           BigDecimal quantity,
                                           BigDecimal previousStock,
                                           BigDecimal newStock,
                                           String reason) {
        InventoryMovement movement = new InventoryMovement();
        movement.setCompany(product.getCompany());
        movement.setProduct(product);
        movement.setUser(user);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(reason);
        inventoryMovementRepository.save(movement);
    }

    private void validateCanEditPurchase(Purchase purchase) {
        if ("ANULADA".equalsIgnoreCase(purchase.getStatus())) {
            throw new IllegalArgumentException("La compra anulada no se puede editar.");
        }
        if (!purchase.getPayments().isEmpty()) {
            throw new IllegalArgumentException("La compra con pagos registrados no se puede editar.");
        }
        if (!"ORDEN".equalsIgnoreCase(purchase.getPurchaseType())) {
            throw new IllegalArgumentException("Solo se pueden editar ordenes de compra.");
        }
        if (!"PENDIENTE".equalsIgnoreCase(purchase.getReceiptStatus())) {
            throw new IllegalArgumentException("Solo se pueden editar ordenes aun no recepcionadas.");
        }
    }

    private void validateDuplicateInvoice(Long companyId, Long supplierId, String invoiceNumber, Long purchaseIdToIgnore) {
        boolean exists = purchaseIdToIgnore == null
                ? purchaseRepository.existsByCompanyIdAndSupplierIdAndInvoiceNumberIgnoreCase(companyId, supplierId, invoiceNumber)
                : purchaseRepository.existsByCompanyIdAndSupplierIdAndInvoiceNumberIgnoreCaseAndIdNot(companyId, supplierId, invoiceNumber, purchaseIdToIgnore);

        if (exists) {
            throw new IllegalArgumentException("Ya existe una compra registrada con ese numero de factura para el proveedor seleccionado.");
        }
    }

    private String normalizePaymentCondition(String paymentCondition) {
        String normalized = paymentCondition == null ? "" : paymentCondition.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CONTADO", "CREDITO" -> normalized;
            default -> throw new IllegalArgumentException("Condicion de pago invalida.");
        };
    }

    private String normalizePurchaseType(String purchaseType) {
        String normalized = purchaseType == null ? "" : purchaseType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "DIRECTA", "ORDEN" -> normalized;
            default -> throw new IllegalArgumentException("Tipo de compra invalido.");
        };
    }

    private String normalizeMethod(String method) {
        String normalized = method == null || method.isBlank() ? "EFECTIVO" : method.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_PAYMENT_METHODS.contains(normalized)) {
            throw new IllegalArgumentException("Metodo de pago invalido.");
        }
        return normalized;
    }

    private String resolvePurchaseStatus(BigDecimal total, BigDecimal paidAmount) {
        BigDecimal normalizedTotal = nvl(total);
        BigDecimal normalizedPaid = nvl(paidAmount);
        if (normalizedPaid.compareTo(BigDecimal.ZERO) == 0) {
            return "PENDIENTE";
        }
        if (normalizedPaid.compareTo(normalizedTotal) >= 0) {
            return "PAGADO";
        }
        return "PARCIAL";
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

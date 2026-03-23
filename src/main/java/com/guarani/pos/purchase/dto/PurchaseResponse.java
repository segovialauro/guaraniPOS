package com.guarani.pos.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponse(
        Long id,
        String purchaseType,
        String receiptStatus,
        String invoiceNumber,
        LocalDate purchaseDate,
        LocalDate dueDate,
        Long supplierId,
        String supplierName,
        String supplierRuc,
        String paymentCondition,
        String status,
        BigDecimal subtotal,
        BigDecimal total,
        BigDecimal paidAmount,
        BigDecimal balance,
        String cancelReason,
        LocalDateTime canceledAt,
        String canceledByName,
        String observation,
        String createdByName,
        LocalDateTime createdAt,
        List<PurchaseDetailResponse> items,
        List<PurchasePaymentResponse> payments
) {
}

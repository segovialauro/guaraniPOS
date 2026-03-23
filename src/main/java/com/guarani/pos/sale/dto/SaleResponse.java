package com.guarani.pos.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        String operationNumber,
        LocalDateTime date,
        Long customerId,
        String customerName,
        String paymentMethod,
        String fiscalDocumentType,
        String fiscalInvoiceNumber,
        String fiscalTimbradoNumber,
        String fiscalEstablishmentCode,
        String fiscalExpeditionPoint,
        String status,
        String cancellationReason,
        LocalDateTime canceledAt,
        String canceledByName,
        String returnReason,
        LocalDateTime returnedAt,
        String returnedByName,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal returnTotal,
        BigDecimal total,
        BigDecimal amountReceived,
        BigDecimal changeDue,
        List<SalePaymentResponse> payments,
        List<SaleDetailResponse> items,
        SaleVatSummaryResponse vatSummary
) {
}

package com.guarani.pos.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleTicketResponse(
        Long saleId,
        String operationNumber,
        String companyName,
        String companyRuc,
        String customerName,
        String cashierName,
        LocalDateTime date,
        String paymentMethod,
        String status,
        String observation,
        BigDecimal total,
        BigDecimal amountReceived,
        BigDecimal changeDue,
        List<SalePaymentResponse> payments,
        List<SaleTicketDetailResponse> items,
        SaleVatSummaryResponse vatSummary
) {
}

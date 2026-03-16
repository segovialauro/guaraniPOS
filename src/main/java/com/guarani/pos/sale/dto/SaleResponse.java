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
        String status,
        BigDecimal total,
        List<SaleDetailResponse> items
) {
}
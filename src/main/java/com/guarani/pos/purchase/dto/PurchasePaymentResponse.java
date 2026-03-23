package com.guarani.pos.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchasePaymentResponse(
        Long id,
        BigDecimal amount,
        String method,
        String reference,
        String observation,
        String createdByName,
        LocalDateTime createdAt
) {
}

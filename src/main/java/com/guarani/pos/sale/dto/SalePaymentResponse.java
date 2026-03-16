package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SalePaymentResponse(
        String method,
        BigDecimal amount,
        String reference
) {
}

package com.guarani.pos.sale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SalePaymentRequest(
        @NotBlank String method,
        @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        String reference
) {
}

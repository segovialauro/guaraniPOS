package com.guarani.pos.cash.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CashOpenRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal openingAmount,

        String observation
) {
}

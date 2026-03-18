package com.guarani.pos.cash.dto;

import com.guarani.pos.cash.model.CashMovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CashMovementRequest(
        @NotNull CashMovementType type,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(max = 500) String description
) {
}

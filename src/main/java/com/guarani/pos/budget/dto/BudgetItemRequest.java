package com.guarani.pos.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetItemRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal quantity
) {
}

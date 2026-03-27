package com.guarani.pos.budget.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BudgetCreateRequest(
        Long customerId,
        LocalDate validUntil,
        String observation,
        @DecimalMin(value = "0.00", inclusive = true) BigDecimal globalDiscountAmount,
        @Valid @NotEmpty List<BudgetItemRequest> items
) {
}

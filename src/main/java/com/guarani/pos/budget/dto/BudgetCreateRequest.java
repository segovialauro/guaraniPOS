package com.guarani.pos.budget.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record BudgetCreateRequest(
        Long customerId,
        LocalDate validUntil,
        String observation,
        @Valid @NotEmpty List<BudgetItemRequest> items
) {
}

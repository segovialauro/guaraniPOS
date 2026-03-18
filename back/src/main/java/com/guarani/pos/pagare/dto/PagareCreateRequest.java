package com.guarani.pos.pagare.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagareCreateRequest(
        @NotNull Long customerId,
        Long saleId,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate dueDate,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        String observation
) {
}

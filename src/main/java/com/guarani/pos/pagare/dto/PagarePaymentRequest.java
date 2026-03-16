package com.guarani.pos.pagare.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagarePaymentRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotBlank String paymentMethod,
        String observation
) {
}

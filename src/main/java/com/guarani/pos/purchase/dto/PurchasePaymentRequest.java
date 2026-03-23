package com.guarani.pos.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PurchasePaymentRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,

        @NotBlank @Size(max = 30)
        String method,

        @Size(max = 100)
        String reference,

        @Size(max = 500)
        String observation
) {
}

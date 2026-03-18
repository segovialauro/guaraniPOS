package com.guarani.pos.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record SaleCreateRequest(
        Long customerId,
        @NotBlank String paymentMethod,
        @DecimalMin(value = "0.00", inclusive = true) BigDecimal amountReceived,
        String observation,
        @Valid List<SalePaymentRequest> payments,
        @Valid @NotEmpty List<SaleItemRequest> items
) {
}
package com.guarani.pos.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaleCreateRequest(
        Long customerId,
        @NotBlank String paymentMethod,
        String observation,
        @Valid @NotEmpty List<SaleItemRequest> items
) {
}
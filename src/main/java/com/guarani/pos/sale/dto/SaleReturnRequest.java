package com.guarani.pos.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaleReturnRequest(
        @NotBlank
        @Size(max = 500)
        String reason,
        @Valid
        @NotEmpty
        List<SaleReturnItemRequest> items
) {
}

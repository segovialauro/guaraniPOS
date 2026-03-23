package com.guarani.pos.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PurchaseCancelRequest(
        @NotBlank
        @Size(max = 500)
        String reason
) {
}

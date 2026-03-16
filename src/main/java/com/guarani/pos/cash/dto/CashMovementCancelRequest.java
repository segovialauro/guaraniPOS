package com.guarani.pos.cash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CashMovementCancelRequest(
        @NotBlank @Size(max = 500) String reason
) {
}

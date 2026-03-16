package com.guarani.pos.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QuickPinRequest(
        @NotBlank
        String tenantCode,

        @NotBlank
        @Pattern(
            regexp = "\\d{4}",
            message = "El PIN debe tener exactamente 4 dígitos"
        )
        String pin
) {}
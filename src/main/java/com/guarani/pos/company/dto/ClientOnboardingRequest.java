package com.guarani.pos.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientOnboardingRequest(
        @NotBlank
        @Size(max = 50)
        String tenantCode,

        @NotBlank
        @Size(max = 150)
        String companyName,

        @Size(max = 20)
        String ruc,

        @NotBlank
        @Size(max = 30)
        String planCode,

        @NotBlank
        @Size(max = 20)
        String adminCedula,

        @NotBlank
        @Size(max = 150)
        String adminFullName,

        @NotBlank
        @Size(min = 6, max = 100)
        String adminPassword,

        @Pattern(regexp = "^$|\\d{4}$", message = "El PIN debe tener 4 digitos.")
        String adminQuickPin
) {
}

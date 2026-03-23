package com.guarani.pos.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserAdminRequest(
        @NotBlank
        @Size(max = 20)
        String cedula,

        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @Pattern(regexp = "^$|\\d{4}$", message = "El PIN debe tener 4 digitos.")
        String quickPin,

        @NotBlank
        @Size(max = 50)
        String roleCode,

        boolean active
) {
}

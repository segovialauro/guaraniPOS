package com.guarani.pos.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientAdminPasswordResetRequest(
        @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Size(min = 6, max = 100, message = "La nueva contraseña debe tener entre 6 y 100 caracteres.")
        String newPassword
) {
}

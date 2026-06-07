package com.guarani.pos.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientOnboardingUpdateRequest(
        @NotBlank(message = "El nombre de la empresa es obligatorio.")
        @Size(max = 50, message = "El nombre de la empresa no puede superar 50 caracteres.")
        String companyName,

        @Size(max = 20, message = "El RUC no puede superar 20 caracteres.")
        String ruc,

        @NotBlank(message = "El plan es obligatorio.")
        String planCode,

        @NotBlank(message = "La cédula del administrador es obligatoria.")
        @Size(max = 20, message = "La cédula no puede superar 20 caracteres.")
        String adminCedula,

        @NotBlank(message = "El nombre del administrador es obligatorio.")
        @Size(max = 50, message = "El nombre del administrador no puede superar 50 caracteres.")
        String adminFullName
) {
}

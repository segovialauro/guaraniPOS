package com.guarani.pos.company.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ClientLicenseUpdateRequest(
        @NotNull(message = "La nueva fecha de vencimiento es obligatoria.")
        LocalDate licenseDueDate) {
}

package com.guarani.pos.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 150)
        String nombre,

        @Size(max = 30)
        String documento,

        @Size(max = 30)
        String ruc,

        @Size(max = 30)
        String telefono,

        @Size(max = 150)
        String email,

        @Size(max = 250)
        String direccion,

        @Size(max = 500)
        String observacion,

        boolean activo
) {
}
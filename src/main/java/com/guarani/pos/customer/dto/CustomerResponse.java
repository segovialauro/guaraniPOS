package com.guarani.pos.customer.dto;

public record CustomerResponse(
        Long id,
        String nombre,
        String documento,
        String documentType,
        String ruc,
        String telefono,
        String email,
        String direccion,
        String gender,
        String segment,
        String taxProfile,
        String observacion,
        boolean activo
) {
}

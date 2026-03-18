package com.guarani.pos.customer.dto;

public record CustomerResponse(
        Long id,
        String nombre,
        String documento,
        String ruc,
        String telefono,
        String email,
        String direccion,
        String observacion,
        boolean activo
) {
}
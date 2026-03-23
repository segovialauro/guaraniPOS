package com.guarani.pos.auth.dto;

public record UserAdminResponse(
        Long id,
        String cedula,
        String fullName,
        String quickPin,
        String roleCode,
        String status
) {
}

package com.guarani.pos.security;

public record JwtUserDetails(
        Long userId,
        Long companyId,
        String cedula,
        String role
) {}

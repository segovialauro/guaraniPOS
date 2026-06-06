package com.guarani.pos.security;

public record JwtUserDetails(
        Long userId,
        Long companyId,
        String tenantCode,
        String datasourceKey,
        String databaseMode,
        String cedula,
        String role
) {}

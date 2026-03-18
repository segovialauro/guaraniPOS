package com.guarani.pos.auth.dto;

public record LoginResponse(
        String token,
        String tenantCode,
        String companyName,
        Long userId,
        String fullName,
        String role
) {}

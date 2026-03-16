package com.guarani.pos.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String tenantCode,
        @NotBlank String cedula,
        @NotBlank String password
) {}

package com.guarani.pos.company.dto;

public record ClientAdminPasswordResetResponse(
        Long companyId,
        String tenantCode,
        String companyName,
        String adminCedula,
        String adminFullName
) {
}

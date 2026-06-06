package com.guarani.pos.company.dto;

public record ClientOnboardingResponse(
        Long companyId,
        String tenantCode,
        String companyName,
        String ruc,
        String planCode,
        String planName,
        String adminCedula,
        String adminFullName,
        String adminRole,
        String status,
        String licenseStatus
) {
}

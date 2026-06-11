package com.guarani.pos.company.dto;

public record ClientAccessUnlockResponse(
        Long companyId,
        String tenantCode,
        String companyName,
        String adminCedula,
        String adminFullName
) {
}

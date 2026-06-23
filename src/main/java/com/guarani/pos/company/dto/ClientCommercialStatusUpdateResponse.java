package com.guarani.pos.company.dto;

public record ClientCommercialStatusUpdateResponse(
        Long companyId,
        String tenantCode,
        String companyName,
        String companyStatus,
        String licenseStatus,
        String subscriptionStatus
) {
}

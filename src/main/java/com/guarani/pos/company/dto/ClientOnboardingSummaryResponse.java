package com.guarani.pos.company.dto;

import java.time.LocalDate;

public record ClientOnboardingSummaryResponse(
        Long companyId,
        String tenantCode,
        String companyName,
        String ruc,
        String planCode,
        String planName,
        String adminCedula,
        String adminFullName,
        String companyStatus,
        String licenseStatus,
        String subscriptionStatus,
        LocalDate licenseDueDate
) {
}

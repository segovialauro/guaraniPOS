package com.guarani.pos.company.dto;

import java.time.LocalDate;

public record ClientLicenseUpdateResponse(
        Long companyId,
        String tenantCode,
        String companyName,
        String companyStatus,
        String licenseStatus,
        LocalDate licenseDueDate) {
}

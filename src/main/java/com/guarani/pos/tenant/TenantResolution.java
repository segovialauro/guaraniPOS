package com.guarani.pos.tenant;

public record TenantResolution(
        Long companyId,
        String tenantCode,
        String companyName,
        TenantDatabaseMode databaseMode,
        String datasourceKey
) {}

package com.guarani.pos.security;

public record TenantContext(
        Long companyId,
        String tenantCode,
        String datasourceKey,
        String databaseMode
) {}

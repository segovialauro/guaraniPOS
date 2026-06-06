package com.guarani.pos.tenant;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.security.SecurityUtils;
import com.guarani.pos.security.TenantContext;

@Service
public class TenantRegistryService {

    private final CompanyRepository companyRepository;
    private final TenantRoutingProperties tenantRoutingProperties;

    public TenantRegistryService(CompanyRepository companyRepository,
                                 TenantRoutingProperties tenantRoutingProperties) {
        this.companyRepository = companyRepository;
        this.tenantRoutingProperties = tenantRoutingProperties;
    }

    @Transactional(readOnly = true)
    public TenantResolution resolveCurrentTenant() {
        TenantContext currentTenant = SecurityUtils.getCurrentTenant();
        return resolveByCompanyIdAndCode(currentTenant.companyId(), currentTenant.tenantCode());
    }

    @Transactional(readOnly = true)
    public TenantResolution resolveByTenantCode(String tenantCode) {
        String normalizedTenantCode = normalizeTenantCode(tenantCode);
        Company company = companyRepository.findByCodeIgnoreCase(normalizedTenantCode)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada para el tenant indicado."));

        return toResolution(company, normalizedTenantCode);
    }

    @Transactional(readOnly = true)
    public TenantResolution resolveByCompanyId(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        return toResolution(company, company.getCode());
    }

    @Transactional(readOnly = true)
    public TenantResolution resolveByCompanyIdAndCode(Long companyId, String tenantCode) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        String normalizedTenantCode = normalizeTenantCode(tenantCode);
        if (!company.getCode().equalsIgnoreCase(normalizedTenantCode)) {
            throw new IllegalArgumentException("El tenant no coincide con la empresa autenticada.");
        }

        return toResolution(company, normalizedTenantCode);
    }

    private TenantResolution toResolution(Company company, String tenantCode) {
        var dedicated = tenantRoutingProperties.getDedicated().get(normalizeTenantCode(tenantCode));

        if (dedicated != null && dedicated.isEnabled()) {
            if (dedicated.getCompanyId() != null && !dedicated.getCompanyId().equals(company.getId())) {
                throw new IllegalStateException(
                        "La configuracion del tenant dedicado no coincide con la empresa registrada."
                );
            }

            String datasourceKey = isBlank(dedicated.getDatasourceKey())
                    ? normalizeTenantCode(tenantCode)
                    : dedicated.getDatasourceKey().trim();

            return new TenantResolution(
                    company.getId(),
                    company.getCode(),
                    company.getName(),
                    TenantDatabaseMode.DEDICATED,
                    datasourceKey
            );
        }

        return new TenantResolution(
                company.getId(),
                company.getCode(),
                company.getName(),
                TenantDatabaseMode.SHARED,
                tenantRoutingProperties.getSharedDatasourceKey()
        );
    }

    private String normalizeTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El tenant es obligatorio.");
        }

        return tenantCode.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

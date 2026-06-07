package com.guarani.pos.company.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.company.dto.ClientAdminPasswordResetRequest;
import com.guarani.pos.company.dto.ClientAdminPasswordResetResponse;
import com.guarani.pos.company.dto.ClientOnboardingRequest;
import com.guarani.pos.company.dto.ClientOnboardingResponse;
import com.guarani.pos.company.dto.ClientOnboardingSummaryResponse;
import com.guarani.pos.company.dto.ClientOnboardingUpdateRequest;
import com.guarani.pos.company.service.ClientOnboardingService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/clientes")
public class ClientOnboardingController {

    private final ClientOnboardingService clientOnboardingService;

    public ClientOnboardingController(ClientOnboardingService clientOnboardingService) {
        this.clientOnboardingService = clientOnboardingService;
    }

    @GetMapping
    public List<ClientOnboardingSummaryResponse> findAll() {
        return clientOnboardingService.findAll(
                SecurityUtils.getCurrentRole(),
                SecurityUtils.getCurrentTenant().tenantCode());
    }

    @PostMapping
    public ClientOnboardingResponse create(@Valid @RequestBody ClientOnboardingRequest request) {
        return clientOnboardingService.create(
                SecurityUtils.getCurrentRole(),
                SecurityUtils.getCurrentTenant().tenantCode(),
                request);
    }

    @PatchMapping("/{companyId}")
    public ClientOnboardingResponse update(
            @PathVariable Long companyId,
            @Valid @RequestBody ClientOnboardingUpdateRequest request) {
        return clientOnboardingService.update(
                SecurityUtils.getCurrentRole(),
                SecurityUtils.getCurrentTenant().tenantCode(),
                companyId,
                request);
    }

    @PatchMapping("/{companyId}/reset-admin-password")
    public ClientAdminPasswordResetResponse resetAdminPassword(
            @PathVariable Long companyId,
            @Valid @RequestBody ClientAdminPasswordResetRequest request) {
        return clientOnboardingService.resetAdminPassword(
                SecurityUtils.getCurrentRole(),
                SecurityUtils.getCurrentTenant().tenantCode(),
                companyId,
                request);
    }
}

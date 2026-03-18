package com.guarani.pos.billing.controller;

import org.springframework.web.bind.annotation.*;

import com.guarani.pos.billing.dto.BillingConfigRequest;
import com.guarani.pos.billing.dto.BillingConfigResponse;
import com.guarani.pos.billing.service.BillingConfigService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/config/billing")
public class BillingConfigController {

    private final BillingConfigService billingConfigService;

    public BillingConfigController(BillingConfigService billingConfigService) {
        this.billingConfigService = billingConfigService;
    }

    @GetMapping
    public BillingConfigResponse getCurrent() {
        return billingConfigService.getCurrent(SecurityUtils.getCurrentCompanyId());
    }

    @PutMapping
    public BillingConfigResponse save(@Valid @RequestBody BillingConfigRequest request) {
        return billingConfigService.save(SecurityUtils.getCurrentCompanyId(), request);
    }
}
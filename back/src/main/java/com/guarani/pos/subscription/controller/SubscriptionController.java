package com.guarani.pos.subscription.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.security.SecurityUtils;
import com.guarani.pos.subscription.dto.SubscriptionFeatureResponse;
import com.guarani.pos.subscription.service.SubscriptionAccessService;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionAccessService subscriptionAccessService;

    public SubscriptionController(SubscriptionAccessService subscriptionAccessService) {
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @GetMapping("/current")
    public SubscriptionFeatureResponse getCurrent() {
        return subscriptionAccessService.getCurrentFeatures(SecurityUtils.getCurrentCompanyId());
    }
}
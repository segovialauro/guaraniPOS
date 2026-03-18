package com.guarani.pos.subscription.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.subscription.dto.PublicPlanResponse;
import com.guarani.pos.subscription.service.SubscriptionAccessService;

@RestController
@RequestMapping("/api/public/plans")
public class PublicPlanController {

    private final SubscriptionAccessService subscriptionAccessService;

    public PublicPlanController(SubscriptionAccessService subscriptionAccessService) {
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @GetMapping
    public List<PublicPlanResponse> list() {
        return subscriptionAccessService.getPublicPlans();
    }
}
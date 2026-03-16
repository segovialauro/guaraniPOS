package com.guarani.pos.dashboard.controller;

import com.guarani.pos.dashboard.dto.DashboardSummaryResponse;
import com.guarani.pos.dashboard.service.DashboardService;
import com.guarani.pos.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.getSummary(SecurityUtils.getCurrentCompanyId());
    }
}

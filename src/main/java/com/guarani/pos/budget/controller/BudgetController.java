package com.guarani.pos.budget.controller;

import com.guarani.pos.budget.dto.BudgetCreateRequest;
import com.guarani.pos.budget.dto.BudgetResponse;
import com.guarani.pos.budget.service.BudgetService;
import com.guarani.pos.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presupuestos")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> findRecent() {
        return budgetService.findRecent(SecurityUtils.getCurrentCompanyId());
    }

    @PostMapping
    public BudgetResponse create(@Valid @RequestBody BudgetCreateRequest request) {
        return budgetService.create(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                request
        );
    }
    @PatchMapping("/{id}/estado")
    public BudgetResponse changeStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        return budgetService.changeStatus(
                SecurityUtils.getCurrentCompanyId(),
                id,
                payload.get("estado")
        );
    }

    @PostMapping("/{id}/convertir")
    public java.util.Map<String, Long> convertToSale(@PathVariable Long id) {
        Long saleId = budgetService.convertToSale(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                id
        );
        return java.util.Map.of("saleId", saleId);
    }

}

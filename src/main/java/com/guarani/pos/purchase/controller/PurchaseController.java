package com.guarani.pos.purchase.controller;

import com.guarani.pos.purchase.dto.*;
import com.guarani.pos.purchase.service.PurchaseService;
import com.guarani.pos.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/resumen")
    public PurchaseSummaryResponse getSummary() {
        return purchaseService.getSummary(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId());
    }

    @GetMapping
    public List<PurchaseResponse> findAll(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to) {
        return purchaseService.findAll(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), q, status, from, to);
    }

    @PostMapping
    public PurchaseResponse create(@Valid @RequestBody PurchaseCreateRequest request) {
        return purchaseService.create(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), request);
    }

    @PutMapping("/{id}")
    public PurchaseResponse update(@PathVariable Long id, @Valid @RequestBody PurchaseUpdateRequest request) {
        return purchaseService.update(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), id, request);
    }

    @PostMapping("/{id}/pagos")
    public PurchaseResponse registerPayment(@PathVariable Long id, @Valid @RequestBody PurchasePaymentRequest request) {
        return purchaseService.registerPayment(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), id, request);
    }

    @PostMapping("/{id}/recepciones")
    public PurchaseResponse receive(@PathVariable Long id, @Valid @RequestBody PurchaseReceiveRequest request) {
        return purchaseService.receive(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), id, request);
    }

    @PostMapping("/{id}/anular")
    public PurchaseResponse cancel(@PathVariable Long id, @Valid @RequestBody PurchaseCancelRequest request) {
        return purchaseService.cancel(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), id, request);
    }
}

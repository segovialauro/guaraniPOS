package com.guarani.pos.electronicinvoice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.electronicinvoice.dto.ElectronicInvoiceResponse;
import com.guarani.pos.electronicinvoice.service.ElectronicInvoiceService;
import com.guarani.pos.security.SecurityUtils;

@RestController
@RequestMapping("/api/electronic-invoices")
public class ElectronicInvoiceController {

    private final ElectronicInvoiceService electronicInvoiceService;

    public ElectronicInvoiceController(ElectronicInvoiceService electronicInvoiceService) {
        this.electronicInvoiceService = electronicInvoiceService;
    }

    @GetMapping
    public List<ElectronicInvoiceResponse> findRecent() {
        return electronicInvoiceService.findRecent(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId()
        );
    }

    @GetMapping("/sale/{saleId}")
    public ElectronicInvoiceResponse findBySale(@PathVariable Long saleId) {
        return electronicInvoiceService.findBySale(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                saleId
        );
    }
}

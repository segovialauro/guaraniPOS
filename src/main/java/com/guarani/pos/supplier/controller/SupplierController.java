package com.guarani.pos.supplier.controller;

import com.guarani.pos.purchase.service.SupplierService;
import com.guarani.pos.security.SecurityUtils;
import com.guarani.pos.supplier.dto.SupplierRequest;
import com.guarani.pos.supplier.dto.SupplierResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponse> findAll(@RequestParam(required = false) String q) {
        return supplierService.findAll(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), q);
    }

    @PostMapping
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), request);
    }
}

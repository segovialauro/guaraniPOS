package com.guarani.pos.customer.controller;

import com.guarani.pos.customer.dto.CustomerRequest;
import com.guarani.pos.customer.dto.CustomerResponse;
import com.guarani.pos.customer.service.CustomerService;
import com.guarani.pos.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> findAll(@RequestParam(required = false) String q) {
        return customerService.findAll(SecurityUtils.getCurrentCompanyId(), q);
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(SecurityUtils.getCurrentCompanyId(), id);
    }

    @PostMapping
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(SecurityUtils.getCurrentCompanyId(), request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(SecurityUtils.getCurrentCompanyId(), id, request);
    }

    @PatchMapping("/{id}/estado")
    public void changeStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        boolean active = Boolean.TRUE.equals(payload.get("activo"));
        customerService.changeStatus(SecurityUtils.getCurrentCompanyId(), id, active);
    }
}
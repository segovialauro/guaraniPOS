package com.guarani.pos.common.parameter.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.common.parameter.dto.AppParameterRequest;
import com.guarani.pos.common.parameter.dto.AppParameterResponse;
import com.guarani.pos.common.parameter.service.AppParameterService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/common/parameters")
public class AppParameterController {

    private final AppParameterService appParameterService;

    public AppParameterController(AppParameterService appParameterService) {
        this.appParameterService = appParameterService;
    }

    @GetMapping
    public List<AppParameterResponse> findByGroupCode(@RequestParam String groupCode) {
        return appParameterService.findByGroupCode(SecurityUtils.getCurrentCompanyId(), groupCode);
    }

    @PostMapping
    public AppParameterResponse create(@Valid @RequestBody AppParameterRequest request) {
        return appParameterService.create(SecurityUtils.getCurrentCompanyId(), request);
    }

    @PutMapping("/{id}")
    public AppParameterResponse update(@PathVariable Long id, @Valid @RequestBody AppParameterRequest request) {
        return appParameterService.update(SecurityUtils.getCurrentCompanyId(), id, request);
    }

    @PatchMapping("/{id}/estado")
    public void changeStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        boolean active = Boolean.TRUE.equals(payload.get("active"));
        appParameterService.changeStatus(SecurityUtils.getCurrentCompanyId(), id, active);
    }
}

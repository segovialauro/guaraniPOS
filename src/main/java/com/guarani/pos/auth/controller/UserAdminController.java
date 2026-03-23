package com.guarani.pos.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.auth.dto.UserAdminRequest;
import com.guarani.pos.auth.dto.UserAdminResponse;
import com.guarani.pos.auth.service.UserAdminService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public List<UserAdminResponse> findAll() {
        validateAdminRole();
        return userAdminService.findAll(SecurityUtils.getCurrentCompanyId());
    }

    @PostMapping
    public UserAdminResponse create(@Valid @RequestBody UserAdminRequest request) {
        validateAdminRole();
        return userAdminService.create(SecurityUtils.getCurrentCompanyId(), request);
    }

    @PatchMapping("/{id}/estado")
    public void changeStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        validateAdminRole();
        boolean active = Boolean.TRUE.equals(payload.get("active"));
        userAdminService.changeStatus(SecurityUtils.getCurrentCompanyId(), id, active);
    }

    private void validateAdminRole() {
        String role = SecurityUtils.getCurrentRole();
        if (role == null || !role.trim().toUpperCase().startsWith("ADMIN")) {
            throw new IllegalArgumentException("Solo un administrador puede gestionar usuarios.");
        }
    }
}

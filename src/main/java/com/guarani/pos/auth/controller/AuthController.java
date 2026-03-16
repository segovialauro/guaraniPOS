package com.guarani.pos.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.auth.dto.CurrentPermissionsResponse;
import com.guarani.pos.auth.dto.LoginRequest;
import com.guarani.pos.auth.dto.LoginResponse;
import com.guarani.pos.auth.dto.QuickPinRequest;
import com.guarani.pos.auth.service.AuthService;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthorizationService authorizationService;

    public AuthController(AuthService authService, AuthorizationService authorizationService) {
        this.authService = authService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/quick-pin")
    public LoginResponse quickPin(@Valid @RequestBody QuickPinRequest request) {
        return authService.quickPin(request);
    }

    @GetMapping("/me/permissions")
    public CurrentPermissionsResponse myPermissions() {
        return new CurrentPermissionsResponse(
                authorizationService.getPermissionsByUserId(SecurityUtils.getCurrentUserId())
        );
    }
}

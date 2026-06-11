package com.guarani.pos.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.auth.dto.CurrentPermissionsResponse;
import com.guarani.pos.auth.dto.ChangeTemporaryPasswordRequest;
import com.guarani.pos.auth.dto.LoginRequest;
import com.guarani.pos.auth.dto.LoginResponse;
import com.guarani.pos.auth.dto.QuickPinRequest;
import com.guarani.pos.auth.service.AuthService;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

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
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, resolveClientIp(httpRequest));
    }

    @PostMapping("/quick-pin")
    public LoginResponse quickPin(@Valid @RequestBody QuickPinRequest request, HttpServletRequest httpRequest) {
        return authService.quickPin(request, resolveClientIp(httpRequest));
    }

    @PostMapping("/change-temporary-password")
    public void changeTemporaryPassword(@Valid @RequestBody ChangeTemporaryPasswordRequest request) {
        authService.changeTemporaryPassword(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                request
        );
    }

    @GetMapping("/me/permissions")
    public CurrentPermissionsResponse myPermissions() {
        return new CurrentPermissionsResponse(
                authorizationService.getPermissionsByUserId(
                        SecurityUtils.getCurrentCompanyId(),
                        SecurityUtils.getCurrentUserId()
                )
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

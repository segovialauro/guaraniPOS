package com.guarani.pos.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.guarani.pos.auth.service.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.guarani.pos.tenant.TenantDataSourceContext;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CompanyAccessGuardService companyAccessGuardService;

    public JwtAuthenticationFilter(JwtService jwtService, CompanyAccessGuardService companyAccessGuardService) {
        this.jwtService = jwtService;
        this.companyAccessGuardService = companyAccessGuardService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.extractAllClaims(token);
            Long userId = ((Number) claims.get("userId")).longValue();
            Long companyId = ((Number) claims.get("companyId")).longValue();
            String tenantCode = claims.get("tenant", String.class);
            String datasourceKey = claims.get("tenantDatasourceKey", String.class);
            String databaseMode = claims.get("tenantDatabaseMode", String.class);
            String cedula = claims.getSubject();
            String role = claims.get("role", String.class);

            JwtUserDetails jwtUserDetails = new JwtUserDetails(
                    userId,
                    companyId,
                    tenantCode,
                    datasourceKey,
                    databaseMode,
                    cedula,
                    role
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            cedula,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            authentication.setDetails(jwtUserDetails);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            TenantDataSourceContext.setCurrentDatasourceKey(datasourceKey);
            companyAccessGuardService.validateAuthenticatedAccess(companyId, userId, tenantCode);
        } catch (Exception ex) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                writeAccessDenied(response, ex.getMessage());
                SecurityContextHolder.clearContext();
                TenantDataSourceContext.clear();
                return;
            }
            SecurityContextHolder.clearContext();
            TenantDataSourceContext.clear();
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantDataSourceContext.clear();
        }
    }

    private void writeAccessDenied(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String safeMessage = message == null || message.isBlank()
                ? "Acceso denegado por politica de licencia o suscripcion."
                : message.replace("\"", "\\\"");
        response.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"" + safeMessage + "\"}");
    }
}

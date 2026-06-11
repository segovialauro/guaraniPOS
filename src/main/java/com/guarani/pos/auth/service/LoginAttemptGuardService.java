package com.guarani.pos.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptGuardService {

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final int loginMaxAttempts;
    private final int quickPinMaxAttempts;
    private final Duration blockDuration;
    private final AuthSecurityAuditService authSecurityAuditService;

    public LoginAttemptGuardService(
            AuthSecurityAuditService authSecurityAuditService,
            @Value("${app.security.login.max-attempts:5}") int loginMaxAttempts,
            @Value("${app.security.quick-pin.max-attempts:4}") int quickPinMaxAttempts,
            @Value("${app.security.auth.block-minutes:15}") long blockMinutes
    ) {
        this.authSecurityAuditService = authSecurityAuditService;
        this.loginMaxAttempts = loginMaxAttempts;
        this.quickPinMaxAttempts = quickPinMaxAttempts;
        this.blockDuration = Duration.ofMinutes(blockMinutes);
    }

    public void checkLoginAllowed(String tenantCode, String cedula, String clientIp) {
        checkAllowed(buildLoginKey(tenantCode, cedula, clientIp));
    }

    public void checkQuickPinAllowed(String tenantCode, String clientIp) {
        checkAllowed(buildQuickPinKey(tenantCode, clientIp));
    }

    public void registerLoginFailure(String tenantCode, String cedula, String clientIp) {
        registerFailure(
                buildLoginKey(tenantCode, cedula, clientIp),
                "LOGIN",
                tenantCode,
                cedula,
                clientIp,
                loginMaxAttempts
        );
    }

    public void registerQuickPinFailure(String tenantCode, String clientIp) {
        registerFailure(
                buildQuickPinKey(tenantCode, clientIp),
                "QUICK_PIN",
                tenantCode,
                null,
                clientIp,
                quickPinMaxAttempts
        );
    }

    public void clearLoginFailures(String tenantCode, String cedula, String clientIp) {
        attempts.remove(buildLoginKey(tenantCode, cedula, clientIp));
    }

    public void clearQuickPinFailures(String tenantCode, String clientIp) {
        attempts.remove(buildQuickPinKey(tenantCode, clientIp));
    }

    public void clearLoginFailuresBySubject(String tenantCode, String cedula) {
        String normalizedTenant = normalize(tenantCode);
        String normalizedCedula = normalize(cedula);
        attempts.keySet().removeIf(key ->
                key.startsWith("LOGIN|" + normalizedTenant + "|" + normalizedCedula + "|"));
    }

    public void clearQuickPinFailuresByTenant(String tenantCode) {
        String normalizedTenant = normalize(tenantCode);
        attempts.keySet().removeIf(key -> key.startsWith("QUICK_PIN|" + normalizedTenant + "|"));
    }

    private void checkAllowed(String key) {
        AttemptState state = attempts.get(key);
        if (state == null) {
            return;
        }

        if (state.blockedUntil() == null) {
            return;
        }

        if (state.blockedUntil().isAfter(Instant.now())) {
            long minutesLeft = Math.max(1, Duration.between(Instant.now(), state.blockedUntil()).toMinutes() + 1);
            authSecurityAuditService.registerPolicyRejection(
                    state.tenantCode(),
                    state.accessChannel(),
                    state.subjectIdentifier(),
                    state.clientIp(),
                    "Intento rechazado mientras el acceso seguia bloqueado."
            );
            throw new IllegalArgumentException(
                    "Demasiados intentos fallidos. Intenta nuevamente en " + minutesLeft + " minuto(s)."
            );
        }

        attempts.remove(key);
    }

    private void registerFailure(
            String key,
            String accessChannel,
            String tenantCode,
            String subjectIdentifier,
            String clientIp,
            int maxAttempts
    ) {
        AttemptState nextState = attempts.compute(key, (_ignored, current) -> {
            AttemptState base = current;
            if (base == null || (base.blockedUntil() != null && base.blockedUntil().isBefore(Instant.now()))) {
                base = new AttemptState(0, null, accessChannel, tenantCode, subjectIdentifier, clientIp);
            }

            int nextFailures = base.failures() + 1;
            if (nextFailures >= maxAttempts) {
                return new AttemptState(
                        nextFailures,
                        Instant.now().plus(blockDuration),
                        accessChannel,
                        tenantCode,
                        subjectIdentifier,
                        clientIp
                );
            }

            return new AttemptState(nextFailures, null, accessChannel, tenantCode, subjectIdentifier, clientIp);
        });

        boolean blocked = nextState.blockedUntil() != null && nextState.blockedUntil().isAfter(Instant.now());
        authSecurityAuditService.registerFailure(
                tenantCode,
                accessChannel,
                subjectIdentifier,
                clientIp,
                nextState.failures(),
                blocked,
                nextState.blockedUntil(),
                blocked
                        ? "Se bloqueo temporalmente el acceso por exceso de intentos fallidos."
                        : "Intento fallido de autenticacion."
        );
    }

    private String buildLoginKey(String tenantCode, String cedula, String clientIp) {
        return "LOGIN|" + normalize(tenantCode) + "|" + normalize(cedula) + "|" + normalize(clientIp);
    }

    private String buildQuickPinKey(String tenantCode, String clientIp) {
        return "QUICK_PIN|" + normalize(tenantCode) + "|" + normalize(clientIp);
    }

    private String normalize(String value) {
        return value == null ? "-" : value.trim().toUpperCase(Locale.ROOT);
    }

    private record AttemptState(
            int failures,
            Instant blockedUntil,
            String accessChannel,
            String tenantCode,
            String subjectIdentifier,
            String clientIp
    ) {
    }
}

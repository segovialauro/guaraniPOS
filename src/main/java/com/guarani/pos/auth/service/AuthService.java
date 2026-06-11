package com.guarani.pos.auth.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.dto.LoginRequest;
import com.guarani.pos.auth.dto.LoginResponse;
import com.guarani.pos.auth.dto.QuickPinRequest;
import com.guarani.pos.auth.dto.ChangeTemporaryPasswordRequest;
import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.tenant.TenantRegistryService;
import com.guarani.pos.tenant.TenantResolution;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TenantRegistryService tenantRegistryService;
    private final TemporaryPasswordService temporaryPasswordService;
    private final LoginAttemptGuardService loginAttemptGuardService;
    private final AuthSecurityAuditService authSecurityAuditService;

    public AuthService(CompanyRepository companyRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TenantRegistryService tenantRegistryService,
                       TemporaryPasswordService temporaryPasswordService,
                       LoginAttemptGuardService loginAttemptGuardService,
                       AuthSecurityAuditService authSecurityAuditService) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tenantRegistryService = tenantRegistryService;
        this.temporaryPasswordService = temporaryPasswordService;
        this.loginAttemptGuardService = loginAttemptGuardService;
        this.authSecurityAuditService = authSecurityAuditService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, String clientIp) {
        String tenantCode = normalizeRequired(request.tenantCode(), "El codigo de empresa es obligatorio.");
        String cedula = normalizeRequired(request.cedula(), "La cedula es obligatoria.");
        String password = normalizeRequired(request.password(), "La contrasena es obligatoria.");

        loginAttemptGuardService.checkLoginAllowed(tenantCode, cedula, clientIp);

        Company company = resolveAndValidateCompany(tenantCode);
        User user = userRepository.findByCompanyIdAndCedula(company.getId(), cedula)
                .orElseThrow(() -> invalidLogin(tenantCode, cedula, clientIp));

        validateActiveUser(user);

        if (!matchesPassword(password, user.getPasswordHash())) {
            throw invalidLogin(tenantCode, cedula, clientIp);
        }

        loginAttemptGuardService.clearLoginFailures(tenantCode, cedula, clientIp);
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse quickPin(QuickPinRequest request, String clientIp) {
        String tenantCode = normalizeRequired(request.tenantCode(), "El codigo de empresa es obligatorio.");
        String pin = normalizeRequired(request.pin(), "El PIN es obligatorio.");

        loginAttemptGuardService.checkQuickPinAllowed(tenantCode, clientIp);

        Company company = resolveAndValidateCompany(tenantCode);
        User user = userRepository.findByCompanyIdAndQuickPin(company.getId(), pin)
                .orElseThrow(() -> invalidQuickPin(tenantCode, clientIp));

        validateActiveUser(user);
        validateQuickPinAccess(user, tenantCode, clientIp);
        loginAttemptGuardService.clearQuickPinFailures(tenantCode, clientIp);
        return buildResponse(user);
    }

    @Transactional
    public void changeTemporaryPassword(Long companyId, Long userId, ChangeTemporaryPasswordRequest request) {
        User user = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        String newPassword = normalizeRequired(request.newPassword(), "La nueva contraseña es obligatoria.");
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        temporaryPasswordService.clearRequired(companyId, userId);
    }

    private Company resolveAndValidateCompany(String tenantCode) {
        TenantResolution tenant = tenantRegistryService.resolveByTenantCode(tenantCode);
        Company company = companyRepository.findById(tenant.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa o licencia no valida."));

        if (!"ACTIVA".equalsIgnoreCase(company.getStatus())) {
            throw new IllegalArgumentException("La empresa se encuentra suspendida o inactiva.");
        }

        if (!"ACTIVA".equalsIgnoreCase(company.getLicenseStatus())) {
            throw new IllegalArgumentException("La licencia no esta activa.");
        }

        if (company.getLicenseDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La licencia esta vencida.");
        }

        return company;
    }

    private void validateActiveUser(User user) {
        if (!"ACTIVO".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("El usuario esta inactivo.");
        }
    }

    private void validateQuickPinAccess(User user, String tenantCode, String clientIp) {
        String roleCode = user.getRoleCode() == null ? "" : user.getRoleCode().trim().toUpperCase();
        if ("ADMIN_EMPRESA".equals(roleCode)) {
            authSecurityAuditService.registerPolicyRejection(
                    tenantCode,
                    "QUICK_PIN",
                    user.getCedula(),
                    clientIp,
                    "Intento de uso de PIN rapido por un usuario administrador."
            );
            throw new IllegalArgumentException("El acceso rapido por PIN solo esta habilitado para cajeros y supervisores.");
        }
    }

    private LoginResponse buildResponse(User user) {
        TenantResolution tenantResolution = tenantRegistryService.resolveByCompanyId(user.getCompany().getId());
        boolean mustChangePassword = temporaryPasswordService.isRequired(user.getCompany().getId(), user.getId());
        return new LoginResponse(
                jwtService.generateToken(user, tenantResolution),
                user.getCompany().getCode(),
                user.getCompany().getName(),
                user.getId(),
                user.getFullName(),
                user.getRoleCode(),
                mustChangePassword
        );
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private IllegalArgumentException invalidLogin(String tenantCode, String cedula, String clientIp) {
        loginAttemptGuardService.registerLoginFailure(tenantCode, cedula, clientIp);
        return new IllegalArgumentException("Usuario o contrasena invalidos.");
    }

    private IllegalArgumentException invalidQuickPin(String tenantCode, String clientIp) {
        loginAttemptGuardService.registerQuickPinFailure(tenantCode, clientIp);
        return new IllegalArgumentException("PIN invalido.");
    }
}

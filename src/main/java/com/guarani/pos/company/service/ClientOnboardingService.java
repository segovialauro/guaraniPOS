package com.guarani.pos.company.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.service.TemporaryPasswordService;
import com.guarani.pos.auth.service.LoginAttemptGuardService;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.company.dto.ClientAccessUnlockResponse;
import com.guarani.pos.company.dto.ClientAdminPasswordResetRequest;
import com.guarani.pos.company.dto.ClientAdminPasswordResetResponse;
import com.guarani.pos.company.dto.ClientCommercialStatusUpdateResponse;
import com.guarani.pos.company.dto.ClientOnboardingRequest;
import com.guarani.pos.company.dto.ClientOnboardingResponse;
import com.guarani.pos.company.dto.ClientOnboardingSummaryResponse;
import com.guarani.pos.company.dto.ClientOnboardingUpdateRequest;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.subscription.model.CompanySubscription;
import com.guarani.pos.subscription.model.SubscriptionPlan;
import com.guarani.pos.subscription.repository.CompanySubscriptionRepository;
import com.guarani.pos.subscription.repository.SubscriptionPlanRepository;

@Service
public class ClientOnboardingService {

    private static final String PRINCIPAL_TENANT_CODE = "principal";

    private final CompanyRepository companyRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordService temporaryPasswordService;
    private final LoginAttemptGuardService loginAttemptGuardService;

    public ClientOnboardingService(
            CompanyRepository companyRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            CompanySubscriptionRepository companySubscriptionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TemporaryPasswordService temporaryPasswordService,
            LoginAttemptGuardService loginAttemptGuardService) {
        this.companyRepository = companyRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.companySubscriptionRepository = companySubscriptionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.temporaryPasswordService = temporaryPasswordService;
        this.loginAttemptGuardService = loginAttemptGuardService;
    }

    @Transactional(readOnly = true)
    public List<ClientOnboardingSummaryResponse> findAll(String currentRole, String currentTenantCode) {
        validateCanManageOnboarding(currentRole, currentTenantCode);

        return companyRepository.findAllByOrderByNameAsc().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional
    public ClientOnboardingResponse create(String currentRole, String currentTenantCode, ClientOnboardingRequest request) {
        validateCanManageOnboarding(currentRole, currentTenantCode);

        String tenantCode = normalizeTenantCode(request.tenantCode());
        if (companyRepository.findByCodeIgnoreCase(tenantCode).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con ese tenant.");
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findByCodeIgnoreCase(normalizeRequired(request.planCode(), "El plan es obligatorio."))
                .filter(SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado o inactivo."));

        Company company = new Company();
        company.setCode(tenantCode);
        company.setName(normalizeRequired(request.companyName(), "El nombre de la empresa es obligatorio."));
        company.setStatus("ACTIVA");
        company.setLicenseStatus("ACTIVA");
        company.setLicenseDueDate(LocalDate.now().plusYears(1));
        company.setRuc(normalizeOptional(request.ruc()));
        company = companyRepository.save(company);

        CompanySubscription subscription = new CompanySubscription();
        subscription.setCompany(company);
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(null);
        subscription.setTrialEndsAt(null);
        subscription.setNotes("Alta inicial desde modulo interno SaaS.");
        companySubscriptionRepository.save(subscription);

        User admin = new User();
        admin.setCompany(company);
        admin.setCedula(normalizeRequired(request.adminCedula(), "La cedula del administrador es obligatoria."));
        admin.setFullName(normalizeRequired(request.adminFullName(), "El nombre del administrador es obligatorio."));
        admin.setPasswordHash(passwordEncoder.encode(request.adminPassword().trim()));
        admin.setQuickPin(normalizeQuickPin(request.adminQuickPin()));
        admin.setRoleCode("ADMIN_EMPRESA");
        admin.setStatus("ACTIVO");
        admin = userRepository.save(admin);
        temporaryPasswordService.markRequired(company, admin);

        return toResponse(company, plan, admin);
    }

    @Transactional
    public ClientOnboardingResponse update(
            String currentRole,
            String currentTenantCode,
            Long companyId,
            ClientOnboardingUpdateRequest request) {
        validateCanManageOnboarding(currentRole, currentTenantCode);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        CompanySubscription subscription = companySubscriptionRepository.findByCompany_IdAndStatus(companyId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("La empresa no tiene un plan activo para actualizar."));

        SubscriptionPlan plan = subscriptionPlanRepository
                .findByCodeIgnoreCase(normalizeRequired(request.planCode(), "El plan es obligatorio."))
                .filter(SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado o inactivo."));

        User admin = userRepository.findFirstByCompanyIdAndRoleCodeOrderByIdAsc(companyId, "ADMIN_EMPRESA")
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un administrador principal para esta empresa."));

        String normalizedCedula = normalizeRequired(request.adminCedula(), "La cédula del administrador es obligatoria.");
        if (userRepository.existsByCompanyIdAndCedulaIgnoreCaseAndIdNot(companyId, normalizedCedula, admin.getId())) {
            throw new IllegalArgumentException("Ya existe otro usuario activo con esa cédula en esta empresa.");
        }

        company.setName(normalizeRequired(request.companyName(), "El nombre de la empresa es obligatorio."));
        company.setRuc(normalizeOptional(request.ruc()));
        companyRepository.save(company);

        subscription.setPlan(plan);
        companySubscriptionRepository.save(subscription);

        admin.setCedula(normalizedCedula);
        admin.setFullName(normalizeRequired(request.adminFullName(), "El nombre del administrador es obligatorio."));
        userRepository.save(admin);

        return toResponse(company, plan, admin);
    }

    @Transactional
    public ClientAdminPasswordResetResponse resetAdminPassword(
            String currentRole,
            String currentTenantCode,
            Long companyId,
            ClientAdminPasswordResetRequest request) {
        validateCanManageOnboarding(currentRole, currentTenantCode);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        User admin = userRepository.findFirstByCompanyIdAndRoleCodeOrderByIdAsc(companyId, "ADMIN_EMPRESA")
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un administrador principal para esta empresa."));

        admin.setPasswordHash(passwordEncoder.encode(normalizeRequired(
                request.newPassword(),
                "La nueva contraseña es obligatoria.")));
        userRepository.save(admin);
        temporaryPasswordService.markRequired(company, admin);

        return new ClientAdminPasswordResetResponse(
                company.getId(),
                company.getCode(),
                company.getName(),
                admin.getCedula(),
                admin.getFullName());
    }

    @Transactional
    public ClientAccessUnlockResponse unlockClientAccess(
            String currentRole,
            String currentTenantCode,
            Long companyId) {
        validateCanManageOnboarding(currentRole, currentTenantCode);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        User admin = userRepository.findFirstByCompanyIdAndRoleCodeOrderByIdAsc(companyId, "ADMIN_EMPRESA")
                .orElseThrow(() -> new IllegalArgumentException("No se encontro un administrador principal para esta empresa."));

        loginAttemptGuardService.clearLoginFailuresBySubject(company.getCode(), admin.getCedula());
        loginAttemptGuardService.clearQuickPinFailuresByTenant(company.getCode());

        return new ClientAccessUnlockResponse(
                company.getId(),
                company.getCode(),
                company.getName(),
                admin.getCedula(),
                admin.getFullName());
    }

    @Transactional
    public ClientCommercialStatusUpdateResponse suspendClient(
            String currentRole,
            String currentTenantCode,
            Long companyId) {
        validateCanManageOnboarding(currentRole, currentTenantCode);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        validateNotPrincipalTenant(company.getCode());

        CompanySubscription subscription = companySubscriptionRepository.findFirstByCompany_IdOrderByStartDateDesc(companyId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no tiene una suscripcion registrada."));

        company.setStatus("SUSPENDIDA");
        company.setLicenseStatus("SUSPENDIDA");
        companyRepository.save(company);

        subscription.setStatus("SUSPENDED");
        companySubscriptionRepository.save(subscription);

        return new ClientCommercialStatusUpdateResponse(
                company.getId(),
                company.getCode(),
                company.getName(),
                company.getStatus(),
                company.getLicenseStatus(),
                subscription.getStatus());
    }

    @Transactional
    public ClientCommercialStatusUpdateResponse reactivateClient(
            String currentRole,
            String currentTenantCode,
            Long companyId) {
        validateCanManageOnboarding(currentRole, currentTenantCode);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        validateNotPrincipalTenant(company.getCode());

        CompanySubscription subscription = companySubscriptionRepository.findFirstByCompany_IdOrderByStartDateDesc(companyId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no tiene una suscripcion registrada."));

        company.setStatus("ACTIVA");
        company.setLicenseStatus("ACTIVA");
        companyRepository.save(company);

        subscription.setStatus("ACTIVE");
        companySubscriptionRepository.save(subscription);

        return new ClientCommercialStatusUpdateResponse(
                company.getId(),
                company.getCode(),
                company.getName(),
                company.getStatus(),
                company.getLicenseStatus(),
                subscription.getStatus());
    }

    private ClientOnboardingSummaryResponse toSummaryResponse(Company company) {
        CompanySubscription subscription = companySubscriptionRepository
                .findFirstByCompany_IdOrderByStartDateDesc(company.getId())
                .orElse(null);
        User admin = userRepository.findFirstByCompanyIdAndRoleCodeOrderByIdAsc(company.getId(), "ADMIN_EMPRESA")
                .orElse(null);

        return new ClientOnboardingSummaryResponse(
                company.getId(),
                company.getCode(),
                company.getName(),
                company.getRuc(),
                subscription != null ? subscription.getPlan().getCode() : "-",
                subscription != null ? subscription.getPlan().getName() : "Sin plan activo",
                admin != null ? admin.getCedula() : "-",
                admin != null ? admin.getFullName() : "-",
                company.getStatus(),
                company.getLicenseStatus(),
                subscription != null ? subscription.getStatus() : "SIN_SUSCRIPCION",
                company.getLicenseDueDate());
    }

    private ClientOnboardingResponse toResponse(Company company, SubscriptionPlan plan, User admin) {
        return new ClientOnboardingResponse(
                company.getId(),
                company.getCode(),
                company.getName(),
                company.getRuc(),
                plan.getCode(),
                plan.getName(),
                admin.getCedula(),
                admin.getFullName(),
                admin.getRoleCode(),
                company.getStatus(),
                company.getLicenseStatus(),
                company.getLicenseDueDate());
    }

    private void validateCanManageOnboarding(String currentRole, String currentTenantCode) {
        String normalizedRole = currentRole == null ? "" : currentRole.trim().toUpperCase(Locale.ROOT);
        String normalizedTenant = currentTenantCode == null ? "" : currentTenantCode.trim().toLowerCase(Locale.ROOT);

        if (!normalizedRole.startsWith("ADMIN")) {
            throw new IllegalArgumentException("Solo un administrador puede dar de alta nuevas empresas.");
        }

        if (!PRINCIPAL_TENANT_CODE.equals(normalizedTenant)) {
            throw new IllegalArgumentException("Solo el tenant principal puede dar de alta nuevas empresas.");
        }
    }

    private void validateNotPrincipalTenant(String tenantCode) {
        if (tenantCode != null && PRINCIPAL_TENANT_CODE.equalsIgnoreCase(tenantCode.trim())) {
            throw new IllegalArgumentException("No se puede suspender ni reactivar el tenant principal desde este modulo.");
        }
    }

    private String normalizeTenantCode(String tenantCode) {
        String normalized = normalizeRequired(tenantCode, "El tenant es obligatorio.")
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_');

        if (!normalized.matches("[a-z0-9_-]{3,50}")) {
            throw new IllegalArgumentException("El tenant debe tener entre 3 y 50 caracteres y solo usar letras, numeros, guion o guion bajo.");
        }

        return normalized;
    }

    private String normalizeQuickPin(String quickPin) {
        if (quickPin == null || quickPin.trim().isEmpty()) {
            return null;
        }

        String normalized = quickPin.trim();
        if (!normalized.matches("\\d{4}")) {
            throw new IllegalArgumentException("El PIN debe tener exactamente 4 digitos.");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

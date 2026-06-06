package com.guarani.pos.company.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.company.dto.ClientOnboardingRequest;
import com.guarani.pos.company.dto.ClientOnboardingResponse;
import com.guarani.pos.company.dto.ClientOnboardingSummaryResponse;
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

    public ClientOnboardingService(
            CompanyRepository companyRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            CompanySubscriptionRepository companySubscriptionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.companySubscriptionRepository = companySubscriptionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
                company.getLicenseStatus());
    }

    private ClientOnboardingSummaryResponse toSummaryResponse(Company company) {
        CompanySubscription subscription = companySubscriptionRepository
                .findFirstByCompany_IdAndStatusOrderByStartDateDesc(company.getId(), "ACTIVE")
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
                company.getLicenseStatus());
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

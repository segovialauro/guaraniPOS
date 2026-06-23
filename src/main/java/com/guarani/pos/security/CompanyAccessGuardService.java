package com.guarani.pos.security;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.subscription.model.CompanySubscription;
import com.guarani.pos.subscription.repository.CompanySubscriptionRepository;

@Service
public class CompanyAccessGuardService {

    private static final String PRINCIPAL_TENANT_CODE = "principal";

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;

    public CompanyAccessGuardService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            CompanySubscriptionRepository companySubscriptionRepository
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companySubscriptionRepository = companySubscriptionRepository;
    }

    @Transactional(readOnly = true)
    public void validateAuthenticatedAccess(Long companyId, Long userId, String tenantCode) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Empresa no encontrada."));

        User user = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado para la empresa autenticada."));

        if (!"ACTIVO".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException("Tu usuario ya no tiene acceso activo.");
        }

        if (!"ACTIVA".equalsIgnoreCase(company.getStatus())) {
            throw new IllegalStateException("La empresa se encuentra suspendida o inactiva.");
        }

        if (!"ACTIVA".equalsIgnoreCase(company.getLicenseStatus())) {
            throw new IllegalStateException("La licencia de la empresa no esta activa.");
        }

        if (company.getLicenseDueDate() == null || company.getLicenseDueDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("La licencia de la empresa esta vencida.");
        }

        if (isPrincipalTenant(tenantCode)) {
            return;
        }

        CompanySubscription subscription = companySubscriptionRepository
                .findFirstByCompany_IdAndStatusInOrderByStartDateDesc(companyId, List.of("ACTIVE", "TRIAL"))
                .orElseThrow(() -> new IllegalStateException("La empresa no tiene una suscripcion habilitada."));

        validateSubscriptionDates(subscription);
    }

    private void validateSubscriptionDates(CompanySubscription subscription) {
        String status = subscription.getStatus() == null ? "" : subscription.getStatus().trim().toUpperCase(Locale.ROOT);

        if ("ACTIVE".equals(status)) {
            if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(LocalDate.now())) {
                throw new IllegalStateException("La suscripcion activa ya vencio.");
            }
            return;
        }

        if ("TRIAL".equals(status)) {
            LocalDateTime trialEndsAt = subscription.getTrialEndsAt();
            if (trialEndsAt != null && trialEndsAt.isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("El periodo de prueba ya vencio.");
            }
            return;
        }

        throw new IllegalStateException("La suscripcion de la empresa no permite el acceso.");
    }

    private boolean isPrincipalTenant(String tenantCode) {
        return tenantCode != null && PRINCIPAL_TENANT_CODE.equalsIgnoreCase(tenantCode.trim());
    }
}

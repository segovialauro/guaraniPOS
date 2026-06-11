package com.guarani.pos.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.AuthSecurityEvent;
import com.guarani.pos.auth.repository.AuthSecurityEventRepository;
import com.guarani.pos.company.repository.CompanyRepository;

@Service
public class AuthSecurityAuditService {

    private final AuthSecurityEventRepository authSecurityEventRepository;
    private final CompanyRepository companyRepository;

    public AuthSecurityAuditService(
            AuthSecurityEventRepository authSecurityEventRepository,
            CompanyRepository companyRepository
    ) {
        this.authSecurityEventRepository = authSecurityEventRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public void registerFailure(
            String tenantCode,
            String accessChannel,
            String subjectIdentifier,
            String clientIp,
            int failureCount,
            boolean blocked,
            Instant blockedUntil,
            String detail
    ) {
        AuthSecurityEvent event = new AuthSecurityEvent();
        event.setCompany(companyRepository.findByCodeIgnoreCase(normalize(tenantCode)).orElse(null));
        event.setTenantCode(normalize(tenantCode));
        event.setAccessChannel(normalize(accessChannel));
        event.setSubjectIdentifier(normalizeSubject(subjectIdentifier));
        event.setClientIp(normalizeSubject(clientIp));
        event.setFailureCount(failureCount);
        event.setBlockedUntil(toLocalDateTime(blockedUntil));
        event.setEventType(blocked ? "ACCESS_BLOCKED" : "AUTH_FAILURE");
        event.setDetail(detail);
        authSecurityEventRepository.save(event);
    }

    @Transactional
    public void registerPolicyRejection(
            String tenantCode,
            String accessChannel,
            String subjectIdentifier,
            String clientIp,
            String detail
    ) {
        AuthSecurityEvent event = new AuthSecurityEvent();
        event.setCompany(companyRepository.findByCodeIgnoreCase(normalize(tenantCode)).orElse(null));
        event.setTenantCode(normalize(tenantCode));
        event.setAccessChannel(normalize(accessChannel));
        event.setSubjectIdentifier(normalizeSubject(subjectIdentifier));
        event.setClientIp(normalizeSubject(clientIp));
        event.setEventType("ACCESS_REJECTED");
        event.setDetail(detail);
        authSecurityEventRepository.save(event);
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSubject(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}

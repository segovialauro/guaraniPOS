package com.guarani.pos.auth.model;

import java.time.LocalDateTime;

import com.guarani.pos.company.model.Company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seguridad_autenticacion_evento")
public class AuthSecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Company company;

    @Column(name = "tenant_code", nullable = false, length = 50)
    private String tenantCode;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "access_channel", nullable = false, length = 20)
    private String accessChannel;

    @Column(name = "subject_identifier", length = 100)
    private String subjectIdentifier;

    @Column(name = "client_ip", length = 100)
    private String clientIp;

    @Column(name = "failure_count")
    private Integer failureCount;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(name = "detail", length = 255)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getAccessChannel() { return accessChannel; }
    public void setAccessChannel(String accessChannel) { this.accessChannel = accessChannel; }
    public String getSubjectIdentifier() { return subjectIdentifier; }
    public void setSubjectIdentifier(String subjectIdentifier) { this.subjectIdentifier = subjectIdentifier; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }
    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

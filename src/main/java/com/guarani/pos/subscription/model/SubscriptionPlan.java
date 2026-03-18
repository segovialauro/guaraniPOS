package com.guarani.pos.subscription.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "suscripcion_plan")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // BASIC, PRO, PREMIUM

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "price_monthly", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceMonthly = BigDecimal.ZERO;

    @Column(name = "max_open_cash_sessions", nullable = false)
    private Integer maxOpenCashSessions = 1;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_branches")
    private Integer maxBranches;

    @Column(name = "allow_fiscal_printer", nullable = false)
    private boolean allowFiscalPrinter = false;

    @Column(name = "allow_electronic_invoice", nullable = false)
    private boolean allowElectronicInvoice = false;

    @Column(name = "allow_internal_ticket", nullable = false)
    private boolean allowInternalTicket = true;

    @Column(name = "allow_bancard_qr", nullable = false)
    private boolean allowBancardQr = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPriceMonthly() {
        return priceMonthly;
    }

    public void setPriceMonthly(BigDecimal priceMonthly) {
        this.priceMonthly = priceMonthly;
    }

    public Integer getMaxOpenCashSessions() {
        return maxOpenCashSessions;
    }

    public void setMaxOpenCashSessions(Integer maxOpenCashSessions) {
        this.maxOpenCashSessions = maxOpenCashSessions;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Integer getMaxBranches() {
        return maxBranches;
    }

    public void setMaxBranches(Integer maxBranches) {
        this.maxBranches = maxBranches;
    }

    public boolean isAllowFiscalPrinter() {
        return allowFiscalPrinter;
    }

    public void setAllowFiscalPrinter(boolean allowFiscalPrinter) {
        this.allowFiscalPrinter = allowFiscalPrinter;
    }

    public boolean isAllowElectronicInvoice() {
        return allowElectronicInvoice;
    }

    public void setAllowElectronicInvoice(boolean allowElectronicInvoice) {
        this.allowElectronicInvoice = allowElectronicInvoice;
    }

    public boolean isAllowInternalTicket() {
        return allowInternalTicket;
    }

    public void setAllowInternalTicket(boolean allowInternalTicket) {
        this.allowInternalTicket = allowInternalTicket;
    }

    public boolean isAllowBancardQr() {
        return allowBancardQr;
    }

    public void setAllowBancardQr(boolean allowBancardQr) {
        this.allowBancardQr = allowBancardQr;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
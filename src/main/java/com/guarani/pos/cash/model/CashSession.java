package com.guarani.pos.cash.model;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.company.model.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_turno")
public class CashSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private User user;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "fecha_cierre")
    private LocalDateTime closedAt;

    @Column(name = "monto_apertura", nullable = false, precision = 15, scale = 2)
    private BigDecimal openingAmount;

    @Column(name = "efectivo_sistema", nullable = false, precision = 15, scale = 2)
    private BigDecimal cashSystem = BigDecimal.ZERO;

    @Column(name = "transferencia_sistema", nullable = false, precision = 15, scale = 2)
    private BigDecimal transferSystem = BigDecimal.ZERO;

    @Column(name = "tarjeta_debito_sistema", nullable = false, precision = 15, scale = 2)
    private BigDecimal debitCardSystem = BigDecimal.ZERO;

    @Column(name = "tarjeta_credito_sistema", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditCardSystem = BigDecimal.ZERO;

    @Column(name = "qr_sistema", nullable = false, precision = 15, scale = 2)
    private BigDecimal qrSystem = BigDecimal.ZERO;

    @Column(name = "total_sistema", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSystem = BigDecimal.ZERO;

    @Column(name = "efectivo_contado", precision = 15, scale = 2)
    private BigDecimal cashCounted;

    @Column(name = "difference", precision = 15, scale = 2)
    private BigDecimal difference;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 500)
    private String observacion;

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public BigDecimal getOpeningAmount() { return openingAmount; }
    public void setOpeningAmount(BigDecimal openingAmount) { this.openingAmount = openingAmount; }
    public BigDecimal getCashSystem() { return cashSystem; }
    public void setCashSystem(BigDecimal cashSystem) { this.cashSystem = cashSystem; }
    public BigDecimal getTransferSystem() { return transferSystem; }
    public void setTransferSystem(BigDecimal transferSystem) { this.transferSystem = transferSystem; }
    public BigDecimal getDebitCardSystem() { return debitCardSystem; }
    public void setDebitCardSystem(BigDecimal debitCardSystem) { this.debitCardSystem = debitCardSystem; }
    public BigDecimal getCreditCardSystem() { return creditCardSystem; }
    public void setCreditCardSystem(BigDecimal creditCardSystem) { this.creditCardSystem = creditCardSystem; }
    public BigDecimal getQrSystem() { return qrSystem; }
    public void setQrSystem(BigDecimal qrSystem) { this.qrSystem = qrSystem; }
    public BigDecimal getTotalSystem() { return totalSystem; }
    public void setTotalSystem(BigDecimal totalSystem) { this.totalSystem = totalSystem; }
    public BigDecimal getCashCounted() { return cashCounted; }
    public void setCashCounted(BigDecimal cashCounted) { this.cashCounted = cashCounted; }
    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}

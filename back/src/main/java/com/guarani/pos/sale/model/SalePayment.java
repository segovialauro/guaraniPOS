package com.guarani.pos.sale.model;

import com.guarani.pos.cash.model.CashSession;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_pago")
public class SalePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_turno_id")
    private CashSession cashSession;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String method;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "referencia", length = 100)
    private String reference;

    public Long getId() { return id; }
    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }
    public CashSession getCashSession() { return cashSession; }
    public void setCashSession(CashSession cashSession) { this.cashSession = cashSession; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}

package com.guarani.pos.budget.model;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.customer.model.Customer;
import com.guarani.pos.sale.model.Sale;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presupuesto")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Customer customer;

    @Column(name = "numero_presupuesto", nullable = false, length = 30)
    private String numeroPresupuesto;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "vigencia_hasta")
    private LocalDate vigenciaHasta;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "subtotal_before_discounts", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotalBeforeDiscounts = BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "global_discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal globalDiscountAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 500)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Sale convertedSale;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetDetail> details = new ArrayList<>();

    public void addDetail(BudgetDetail detail) {
        detail.setBudget(this);
        this.details.add(detail);
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getNumeroPresupuesto() { return numeroPresupuesto; }
    public void setNumeroPresupuesto(String numeroPresupuesto) { this.numeroPresupuesto = numeroPresupuesto; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public LocalDate getVigenciaHasta() { return vigenciaHasta; }
    public void setVigenciaHasta(LocalDate vigenciaHasta) { this.vigenciaHasta = vigenciaHasta; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getSubtotalBeforeDiscounts() { return subtotalBeforeDiscounts; }
    public void setSubtotalBeforeDiscounts(BigDecimal subtotalBeforeDiscounts) { this.subtotalBeforeDiscounts = subtotalBeforeDiscounts; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(BigDecimal discountTotal) { this.discountTotal = discountTotal; }
    public BigDecimal getGlobalDiscountAmount() { return globalDiscountAmount; }
    public void setGlobalDiscountAmount(BigDecimal globalDiscountAmount) { this.globalDiscountAmount = globalDiscountAmount; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Sale getConvertedSale() { return convertedSale; }
    public void setConvertedSale(Sale convertedSale) { this.convertedSale = convertedSale; }
    public List<BudgetDetail> getDetails() { return details; }
}

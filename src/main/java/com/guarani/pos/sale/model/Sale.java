package com.guarani.pos.sale.model;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.customer.model.Customer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Customer customer;

    @Column(name = "numero_operacion", nullable = false, length = 30)
    private String numeroOperacion;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 500)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id")
    private User createdBy;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();

    public void addDetail(SaleDetail detail) {
        detail.setSale(this);
        this.details.add(detail);
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public List<SaleDetail> getDetails() { return details; }
}
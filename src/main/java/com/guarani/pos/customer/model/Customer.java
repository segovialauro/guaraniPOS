package com.guarani.pos.customer.model;

import com.guarani.pos.company.model.Company;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Company company;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 30)
    private String documento;

    @Column(length = 30)
    private String ruc;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(length = 250)
    private String direccion;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
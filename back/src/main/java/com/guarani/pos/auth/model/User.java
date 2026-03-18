package com.guarani.pos.auth.model;

import com.guarani.pos.company.model.Company;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Company company;

    @Column(name = "cedula", nullable = false, length = 20)
    private String cedula;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String fullName;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "quick_pin", length = 4)
    private String quickPin;

    @Column(name = "rol_codigo", nullable = false, length = 50)
    private String roleCode;

    @Column(name = "estado", nullable = false, length = 20)
    private String status;

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public String getCedula() { return cedula; }
    public String getFullName() { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public String getQuickPin() { return quickPin; }
    public String getRoleCode() { return roleCode; }
    public String getStatus() { return status; }
}

package com.guarani.pos.company.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "empresa")
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "codigo", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "nombre", nullable = false, length = 150)
	private String name;

	@Column(name = "estado", nullable = false, length = 20)
	private String status;

	@Column(name = "licencia_estado", nullable = false, length = 20)
	private String licenseStatus;

	@Column(name = "licencia_vencimiento", nullable = false)
	private LocalDate licenseDueDate;
	@Column(name = "ruc", length = 20)
	private String ruc;

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public String getLicenseStatus() {
		return licenseStatus;
	}

	public LocalDate getLicenseDueDate() {
		return licenseDueDate;
	}

	public String getRuc() {
		return ruc;
	}

	public void setRuc(String ruc) {
		this.ruc = ruc;
	}
}

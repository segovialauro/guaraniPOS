package com.guarani.pos.billing.model;

import java.time.LocalDateTime;

import com.guarani.pos.company.model.Company;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_facturacion")
public class BillingConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id")
	private Company company;

	@Column(name = "document_type", nullable = false, length = 30)
	private String documentType; // INTERNO, FISCAL_PRINTER, ELECTRONICO

	@Column(name = "printer_brand", length = 100)
	private String printerBrand;

	@Column(name = "printer_model", length = 100)
	private String printerModel;

	@Column(name = "printer_name", length = 150)
	private String printerName;

	@Column(name = "establishment_code", length = 20)
	private String establishmentCode;

	@Column(name = "expedition_point", length = 20)
	private String expeditionPoint;

	@Column(name = "invoice_footer", length = 500)
	private String invoiceFooter;

	@Column(name = "sifen_environment", length = 20)
	private String sifenEnvironment;

	@Column(name = "active", nullable = false)
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

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getPrinterBrand() {
		return printerBrand;
	}

	public void setPrinterBrand(String printerBrand) {
		this.printerBrand = printerBrand;
	}

	public String getPrinterModel() {
		return printerModel;
	}

	public void setPrinterModel(String printerModel) {
		this.printerModel = printerModel;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public String getEstablishmentCode() {
		return establishmentCode;
	}

	public void setEstablishmentCode(String establishmentCode) {
		this.establishmentCode = establishmentCode;
	}

	public String getExpeditionPoint() {
		return expeditionPoint;
	}

	public void setExpeditionPoint(String expeditionPoint) {
		this.expeditionPoint = expeditionPoint;
	}

	public String getInvoiceFooter() {
		return invoiceFooter;
	}

	public void setInvoiceFooter(String invoiceFooter) {
		this.invoiceFooter = invoiceFooter;
	}

	public String getSifenEnvironment() {
		return sifenEnvironment;
	}

	public void setSifenEnvironment(String sifenEnvironment) {
		this.sifenEnvironment = sifenEnvironment;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
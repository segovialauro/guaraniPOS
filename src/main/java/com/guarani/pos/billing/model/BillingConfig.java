package com.guarani.pos.billing.model;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

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
    private String documentType;

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

    @Column(name = "commercial_name", length = 150)
    private String commercialName;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(name = "ruc", length = 30)
    private String ruc;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", length = 250)
    private String address;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "timbrado_number", length = 50)
    private String timbradoNumber;

    @Column(name = "timbrado_validity", length = 50)
    private String timbradoValidity;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "logo_data_url", columnDefinition = "TEXT")
    private String logoDataUrl;

    @Column(name = "show_seller", nullable = false)
    private boolean showSeller = true;

    @Column(name = "show_vat_breakdown", nullable = false)
    private boolean showVatBreakdown = false;

    @Column(name = "show_set_qr", nullable = false)
    private boolean showSetQr = false;

    @Column(name = "show_item_discount", nullable = false)
    private boolean showItemDiscount = false;

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

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getTimbradoNumber() {
        return timbradoNumber;
    }

    public void setTimbradoNumber(String timbradoNumber) {
        this.timbradoNumber = timbradoNumber;
    }

    public String getTimbradoValidity() {
        return timbradoValidity;
    }

    public void setTimbradoValidity(String timbradoValidity) {
        this.timbradoValidity = timbradoValidity;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getLogoDataUrl() {
        return logoDataUrl;
    }

    public void setLogoDataUrl(String logoDataUrl) {
        this.logoDataUrl = logoDataUrl;
    }

    public boolean isShowSeller() {
        return showSeller;
    }

    public void setShowSeller(boolean showSeller) {
        this.showSeller = showSeller;
    }

    public boolean isShowVatBreakdown() {
        return showVatBreakdown;
    }

    public void setShowVatBreakdown(boolean showVatBreakdown) {
        this.showVatBreakdown = showVatBreakdown;
    }

    public boolean isShowSetQr() {
        return showSetQr;
    }

    public void setShowSetQr(boolean showSetQr) {
        this.showSetQr = showSetQr;
    }

    public boolean isShowItemDiscount() {
        return showItemDiscount;
    }

    public void setShowItemDiscount(boolean showItemDiscount) {
        this.showItemDiscount = showItemDiscount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

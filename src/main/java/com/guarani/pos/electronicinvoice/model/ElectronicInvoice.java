package com.guarani.pos.electronicinvoice.model;

import java.time.LocalDateTime;

import com.guarani.pos.billing.model.BillingConfig;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.sale.model.Sale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "factura_electronica")
public class ElectronicInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Company company;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuracion_facturacion_id")
    private BillingConfig billingConfig;

    @Column(name = "version_formato", nullable = false, length = 10)
    private String formatVersion = "150";

    @Column(name = "document_type_code", nullable = false, length = 2)
    private String documentTypeCode;

    @Column(name = "document_type_description", nullable = false, length = 60)
    private String documentTypeDescription;

    @Column(name = "emission_type_code", nullable = false, length = 1)
    private String emissionTypeCode = "1";

    @Column(name = "emission_type_description", nullable = false, length = 20)
    private String emissionTypeDescription = "Normal";

    @Column(name = "taxpayer_type", nullable = false, length = 1)
    private String taxpayerType;

    @Column(length = 2)
    private String series;

    @Column(name = "document_number", nullable = false, length = 7)
    private String documentNumber;

    @Column(nullable = false, length = 44, unique = true)
    private String cdc;

    @Column(name = "cdc_check_digit", nullable = false, length = 1)
    private String cdcCheckDigit;

    @Column(name = "security_code", nullable = false, length = 9)
    private String securityCode;

    @Column(name = "digest_value", length = 500)
    private String digestValue;

    @Column(name = "qr_payload", columnDefinition = "TEXT")
    private String qrPayload;

    @Column(name = "qr_url", columnDefinition = "TEXT")
    private String qrUrl;

    @Column(name = "unsigned_xml", nullable = false, columnDefinition = "TEXT")
    private String unsignedXml;

    @Column(name = "signed_xml", columnDefinition = "TEXT")
    private String signedXml;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(name = "status_detail", length = 500)
    private String statusDetail;

    @Column(nullable = false, length = 20)
    private String environment;

    @Column(name = "batch_number", length = 20)
    private String batchNumber;

    @Column(name = "transaction_number", length = 20)
    private String transactionNumber;

    @Column(name = "reception_status_code", length = 10)
    private String receptionStatusCode;

    @Column(name = "reception_status_message", length = 255)
    private String receptionStatusMessage;

    @Column(name = "processing_status_code", length = 10)
    private String processingStatusCode;

    @Column(name = "processing_status_message", length = 255)
    private String processingStatusMessage;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }
    public BillingConfig getBillingConfig() { return billingConfig; }
    public void setBillingConfig(BillingConfig billingConfig) { this.billingConfig = billingConfig; }
    public String getFormatVersion() { return formatVersion; }
    public void setFormatVersion(String formatVersion) { this.formatVersion = formatVersion; }
    public String getDocumentTypeCode() { return documentTypeCode; }
    public void setDocumentTypeCode(String documentTypeCode) { this.documentTypeCode = documentTypeCode; }
    public String getDocumentTypeDescription() { return documentTypeDescription; }
    public void setDocumentTypeDescription(String documentTypeDescription) { this.documentTypeDescription = documentTypeDescription; }
    public String getEmissionTypeCode() { return emissionTypeCode; }
    public void setEmissionTypeCode(String emissionTypeCode) { this.emissionTypeCode = emissionTypeCode; }
    public String getEmissionTypeDescription() { return emissionTypeDescription; }
    public void setEmissionTypeDescription(String emissionTypeDescription) { this.emissionTypeDescription = emissionTypeDescription; }
    public String getTaxpayerType() { return taxpayerType; }
    public void setTaxpayerType(String taxpayerType) { this.taxpayerType = taxpayerType; }
    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getCdc() { return cdc; }
    public void setCdc(String cdc) { this.cdc = cdc; }
    public String getCdcCheckDigit() { return cdcCheckDigit; }
    public void setCdcCheckDigit(String cdcCheckDigit) { this.cdcCheckDigit = cdcCheckDigit; }
    public String getSecurityCode() { return securityCode; }
    public void setSecurityCode(String securityCode) { this.securityCode = securityCode; }
    public String getDigestValue() { return digestValue; }
    public void setDigestValue(String digestValue) { this.digestValue = digestValue; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }
    public String getUnsignedXml() { return unsignedXml; }
    public void setUnsignedXml(String unsignedXml) { this.unsignedXml = unsignedXml; }
    public String getSignedXml() { return signedXml; }
    public void setSignedXml(String signedXml) { this.signedXml = signedXml; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusDetail() { return statusDetail; }
    public void setStatusDetail(String statusDetail) { this.statusDetail = statusDetail; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getTransactionNumber() { return transactionNumber; }
    public void setTransactionNumber(String transactionNumber) { this.transactionNumber = transactionNumber; }
    public String getReceptionStatusCode() { return receptionStatusCode; }
    public void setReceptionStatusCode(String receptionStatusCode) { this.receptionStatusCode = receptionStatusCode; }
    public String getReceptionStatusMessage() { return receptionStatusMessage; }
    public void setReceptionStatusMessage(String receptionStatusMessage) { this.receptionStatusMessage = receptionStatusMessage; }
    public String getProcessingStatusCode() { return processingStatusCode; }
    public void setProcessingStatusCode(String processingStatusCode) { this.processingStatusCode = processingStatusCode; }
    public String getProcessingStatusMessage() { return processingStatusMessage; }
    public void setProcessingStatusMessage(String processingStatusMessage) { this.processingStatusMessage = processingStatusMessage; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(LocalDateTime canceledAt) { this.canceledAt = canceledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

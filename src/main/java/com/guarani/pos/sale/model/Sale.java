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

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal descuentoTotal = BigDecimal.ZERO;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "monto_recibido", precision = 15, scale = 2)
    private BigDecimal montoRecibido;

    @Column(name = "vuelto", precision = 15, scale = 2)
    private BigDecimal vuelto;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 500)
    private String observacion;

    @Column(name = "motivo_anulacion", length = 500)
    private String cancellationReason;

    @Column(name = "fecha_anulacion")
    private LocalDateTime canceledAt;

    @Column(name = "motivo_devolucion", length = 500)
    private String returnReason;

    @Column(name = "fecha_devolucion")
    private LocalDateTime returnedAt;

    @Column(name = "monto_devolucion", nullable = false, precision = 15, scale = 2)
    private BigDecimal returnTotal = BigDecimal.ZERO;

    @Column(name = "fiscal_document_type", length = 30)
    private String fiscalDocumentType;

    @Column(name = "fiscal_invoice_number", length = 50)
    private String fiscalInvoiceNumber;

    @Column(name = "fiscal_timbrado_number", length = 50)
    private String fiscalTimbradoNumber;

    @Column(name = "fiscal_establishment_code", length = 20)
    private String fiscalEstablishmentCode;

    @Column(name = "fiscal_expedition_point", length = 20)
    private String fiscalExpeditionPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_usuario_id")
    private User canceledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devuelto_por_usuario_id")
    private User returnedBy;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalePayment> payments = new ArrayList<>();

    public void addDetail(SaleDetail detail) {
        detail.setSale(this);
        this.details.add(detail);
    }

    public void addPayment(SalePayment payment) {
        payment.setSale(this);
        this.payments.add(payment);
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
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDescuentoTotal() { return descuentoTotal; }
    public void setDescuentoTotal(BigDecimal descuentoTotal) { this.descuentoTotal = descuentoTotal; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }
    public BigDecimal getVuelto() { return vuelto; }
    public void setVuelto(BigDecimal vuelto) { this.vuelto = vuelto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(LocalDateTime canceledAt) { this.canceledAt = canceledAt; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public BigDecimal getReturnTotal() { return returnTotal; }
    public void setReturnTotal(BigDecimal returnTotal) { this.returnTotal = returnTotal; }
    public String getFiscalDocumentType() { return fiscalDocumentType; }
    public void setFiscalDocumentType(String fiscalDocumentType) { this.fiscalDocumentType = fiscalDocumentType; }
    public String getFiscalInvoiceNumber() { return fiscalInvoiceNumber; }
    public void setFiscalInvoiceNumber(String fiscalInvoiceNumber) { this.fiscalInvoiceNumber = fiscalInvoiceNumber; }
    public String getFiscalTimbradoNumber() { return fiscalTimbradoNumber; }
    public void setFiscalTimbradoNumber(String fiscalTimbradoNumber) { this.fiscalTimbradoNumber = fiscalTimbradoNumber; }
    public String getFiscalEstablishmentCode() { return fiscalEstablishmentCode; }
    public void setFiscalEstablishmentCode(String fiscalEstablishmentCode) { this.fiscalEstablishmentCode = fiscalEstablishmentCode; }
    public String getFiscalExpeditionPoint() { return fiscalExpeditionPoint; }
    public void setFiscalExpeditionPoint(String fiscalExpeditionPoint) { this.fiscalExpeditionPoint = fiscalExpeditionPoint; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public User getCanceledBy() { return canceledBy; }
    public void setCanceledBy(User canceledBy) { this.canceledBy = canceledBy; }
    public User getReturnedBy() { return returnedBy; }
    public void setReturnedBy(User returnedBy) { this.returnedBy = returnedBy; }
    public List<SaleDetail> getDetails() { return details; }
    public List<SalePayment> getPayments() { return payments; }
}

package com.guarani.pos.pagare.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagare_pago")
public class PagarePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pagare_id")
    private Pagare pagare;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(length = 500)
    private String observacion;

    public Long getId() { return id; }
    public Pagare getPagare() { return pagare; }
    public void setPagare(Pagare pagare) { this.pagare = pagare; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}

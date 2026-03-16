package com.guarani.pos.product.model;

import com.guarani.pos.company.model.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id")
	private Company company;

	@Column(nullable = false, length = 50)
	private String codigo;

	@Column(nullable = false, length = 150)
	private String nombre;

	@Column(length = 500)
	private String descripcion;

	@Column(length = 100)
	private String categoria;

	@Column(name = "precio_costo", nullable = false, precision = 15, scale = 2)
	private BigDecimal precioCosto = BigDecimal.ZERO;

	@Column(name = "precio_venta", nullable = false, precision = 15, scale = 2)
	private BigDecimal precioVenta;

	@Column(name = "stock_actual", nullable = false, precision = 15, scale = 2)
	private BigDecimal stockActual = BigDecimal.ZERO;

	@Column(name = "stock_minimo", nullable = false, precision = 15, scale = 2)
	private BigDecimal stockMinimo = BigDecimal.ZERO;

	@Column(name = "unidad_medida", nullable = false, length = 30)
	private String unidadMedida = "UNIDAD";

	@Column(nullable = false)
	private boolean activo = true;

	@Column(name = "creado_en", nullable = false)
	private LocalDateTime creadoEn = LocalDateTime.now();

	@Column(name = "actualizado_en", nullable = false)
	private LocalDateTime actualizadoEn = LocalDateTime.now();

	@Column(name = "qr_contenido", length = 255)
	private String qrContenido;
	
	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(String codigoBarras) {
		this.codigoBarras = codigoBarras;
	}

	@Column(name = "codigo_barras", length = 100)
	private String codigoBarras;


	@PreUpdate
	public void preUpdate() {
		this.actualizadoEn = LocalDateTime.now();
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

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public BigDecimal getPrecioCosto() {
		return precioCosto;
	}

	public void setPrecioCosto(BigDecimal precioCosto) {
		this.precioCosto = precioCosto;
	}

	public BigDecimal getPrecioVenta() {
		return precioVenta;
	}

	public void setPrecioVenta(BigDecimal precioVenta) {
		this.precioVenta = precioVenta;
	}

	public BigDecimal getStockActual() {
		return stockActual;
	}

	public void setStockActual(BigDecimal stockActual) {
		this.stockActual = stockActual;
	}

	public BigDecimal getStockMinimo() {
		return stockMinimo;
	}

	public void setStockMinimo(BigDecimal stockMinimo) {
		this.stockMinimo = stockMinimo;
	}

	public String getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public LocalDateTime getCreadoEn() {
		return creadoEn;
	}

	public void setCreadoEn(LocalDateTime creadoEn) {
		this.creadoEn = creadoEn;
	}

	public LocalDateTime getActualizadoEn() {
		return actualizadoEn;
	}

	public void setActualizadoEn(LocalDateTime actualizadoEn) {
		this.actualizadoEn = actualizadoEn;
	}

	public String getQrContenido() {
		return qrContenido;
	}

	public void setQrContenido(String qrContenido) {
		this.qrContenido = qrContenido;
	}
}
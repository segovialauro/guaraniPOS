package com.guarani.pos.product.model;

import com.guarani.pos.company.model.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Company company;

    @Column(name = "producto_nombre", nullable = false, length = 150)
    private String productName;

    @Column(name = "current_stock", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentStock;

    @Column(name = "min_stock", nullable = false, precision = 15, scale = 2)
    private BigDecimal minStock;

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public BigDecimal getMinStock() { return minStock; }
    public void setMinStock(BigDecimal minStock) { this.minStock = minStock; }
}

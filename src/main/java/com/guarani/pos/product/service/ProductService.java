package com.guarani.pos.product.service;

import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.product.dto.ProductRequest;
import com.guarani.pos.product.dto.ProductResponse;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    public ProductService(ProductRepository productRepository,
                          CompanyRepository companyRepository) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(Long companyId, String q) {
        List<Product> products = (q == null || q.isBlank())
                ? productRepository.findByCompanyIdOrderByNombreAsc(companyId)
                : productRepository.findByCompanyIdAndNombreContainingIgnoreCaseOrderByNombreAsc(companyId, q.trim());

        return products.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long companyId, Long id) {
        Product product = productRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(Long companyId, ProductRequest request) {

        if (productRepository.existsByCompanyIdAndCodigoIgnoreCase(companyId, request.codigo())) {
            throw new IllegalArgumentException("Ya existe un producto con ese código.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        Product product = new Product();
        product.setCompany(company);
        apply(product, request);

        product = productRepository.save(product);

        product.setQrContenido("http://localhost:4200/productos/" + product.getId() + "/qr-view");
        product = productRepository.save(product);

        return toResponse(product);      
    }

    @Transactional
    public ProductResponse update(Long companyId, Long id, ProductRequest request) {
        Product product = productRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        if (productRepository.existsByCompanyIdAndCodigoIgnoreCaseAndIdNot(companyId, request.codigo(), id)) {
            throw new IllegalArgumentException("Ya existe otro producto con ese código.");
        }

        apply(product, request);

        if (product.getQrContenido() == null || product.getQrContenido().isBlank()) {
            product.setQrContenido("http://localhost:4200/productos/" + product.getId() + "/qr-view");
        }

        return toResponse(productRepository.save(product));
    }

    
    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(Long companyId, String barcode) {
        Product product = productRepository.findByCompanyIdAndCodigoBarras(companyId, barcode)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        return toResponse(product);
    }

    @Transactional
    public void changeStatus(Long companyId, Long id, boolean active) {
        Product product = productRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        product.setActivo(active);
        productRepository.save(product);
    }

    private void apply(Product product, ProductRequest request) {
        product.setCodigo(request.codigo().trim());
        product.setNombre(request.nombre().trim());
        product.setDescripcion(request.descripcion());
        product.setCategoria(request.categoria());
        product.setPrecioCosto(request.precioCosto());
        product.setPrecioVenta(request.precioVenta());
        product.setStockActual(request.stockActual());
        product.setStockMinimo(request.stockMinimo());
        product.setUnidadMedida(request.unidadMedida().trim());
        product.setVatType(normalizeVatType(request.vatType()));
        product.setActivo(request.activo());
        product.setCodigoBarras(request.codigoBarras());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCodigo(),
                product.getNombre(),
                product.getDescripcion(),
                product.getCategoria(),
                product.getPrecioCosto(),
                product.getPrecioVenta(),
                product.getStockActual(),
                product.getStockMinimo(),
                product.getUnidadMedida(),
                product.getVatType(),
                product.isActivo(),
                product.getQrContenido(),
                product.getCodigoBarras()
        );
    }

    private String normalizeVatType(String vatType) {
        String normalized = vatType == null ? "" : vatType.trim().toUpperCase();
        return switch (normalized) {
            case "IVA_10", "IVA_5", "EXENTO" -> normalized;
            default -> throw new IllegalArgumentException("Tipo de IVA inv\u00e1lido.");
        };
    }
}

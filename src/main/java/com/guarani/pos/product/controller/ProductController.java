package com.guarani.pos.product.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.guarani.pos.product.dto.ProductImportResponse;
import com.guarani.pos.product.dto.ProductRequest;
import com.guarani.pos.product.dto.ProductResponse;
import com.guarani.pos.product.service.ProductService;
import com.guarani.pos.product.service.QrCodeService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductService productService;
    private final QrCodeService qrCodeService;
    
    public ProductController(ProductService productService,QrCodeService qrCodeService ) {
        this.productService = productService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    public List<ProductResponse> findAll(@RequestParam(required = false) String q) {
        return productService.findAll(SecurityUtils.getCurrentCompanyId(), q);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(SecurityUtils.getCurrentCompanyId(), id);
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(SecurityUtils.getCurrentCompanyId(), request);
    }

    @GetMapping("/plantilla-excel")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        byte[] content = productService.exportExcelTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_productos.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @PostMapping("/importar-excel")
    public ProductImportResponse importExcel(@RequestParam("file") MultipartFile file) {
        return productService.importFromExcel(SecurityUtils.getCurrentCompanyId(), file);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(SecurityUtils.getCurrentCompanyId(), id, request);
    }

    @PatchMapping("/{id}/estado")
    public void changeStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        boolean active = Boolean.TRUE.equals(payload.get("activo"));
        productService.changeStatus(SecurityUtils.getCurrentCompanyId(), id, active);
    }
    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> getQr(@PathVariable Long id) {

        ProductResponse product =
            productService.findById(SecurityUtils.getCurrentCompanyId(), id);

        byte[] png = qrCodeService.generateQrPng(product.qrContenido(), 250, 250);

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(png);
    }
    @GetMapping("/barcode/{barcode}")
    public ProductResponse findByBarcode(@PathVariable String barcode) {
        return productService.findByBarcode(SecurityUtils.getCurrentCompanyId(), barcode);
    }

}

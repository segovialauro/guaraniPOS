package com.guarani.pos.product.service;

import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.product.dto.ProductImportResponse;
import com.guarani.pos.product.dto.ProductRequest;
import com.guarani.pos.product.dto.ProductResponse;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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

    @Transactional(readOnly = true)
    public byte[] exportExcelTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");

            Row header = sheet.createRow(0);
            String[] columns = {
                    "codigo",
                    "nombre",
                    "descripcion",
                    "categoria",
                    "precioCosto",
                    "precioVenta",
                    "precioVentaMayorista",
                    "cantidadMayoristaMinima",
                    "stockActual",
                    "stockMinimo",
                    "unidadMedida",
                    "vatType",
                    "codigoBarras",
                    "activo"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("PEL-001");
            sample.createCell(1).setCellValue("Pelota clasica");
            sample.createCell(2).setCellValue("Producto de ejemplo");
            sample.createCell(3).setCellValue("DEPORTES");
            sample.createCell(4).setCellValue(15000);
            sample.createCell(5).setCellValue(25000);
            sample.createCell(6).setCellValue(20000);
            sample.createCell(7).setCellValue(3);
            sample.createCell(8).setCellValue(20);
            sample.createCell(9).setCellValue(5);
            sample.createCell(10).setCellValue("UNIDAD");
            sample.createCell(11).setCellValue("IVA_10");
            sample.createCell(12).setCellValue("1234567890123");
            sample.createCell(13).setCellValue("true");

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar la plantilla Excel de productos.", e);
        }
    }

    @Transactional
    public ProductImportResponse importFromExcel(Long companyId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar un archivo Excel.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        int imported = 0;
        int skipped = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 0
                    ? workbook.getSheetAt(0)
                    : null;

            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                throw new IllegalArgumentException("El archivo Excel no tiene filas para importar.");
            }

            DataFormatter formatter = new DataFormatter();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                String codigo = readCell(row, 0, formatter);
                if (codigo == null || codigo.isBlank()) {
                    skipped++;
                    continue;
                }

                if (productRepository.existsByCompanyIdAndCodigoIgnoreCase(companyId, codigo.trim())) {
                    skipped++;
                    continue;
                }

                Product product = new Product();
                product.setCompany(company);
                product.setCodigo(codigo.trim());
                product.setNombre(readCell(row, 1, formatter));
                product.setDescripcion(trimToNull(readCell(row, 2, formatter)));
                product.setCategoria(trimToNull(readCell(row, 3, formatter)));
                product.setPrecioCosto(readDecimal(row, 4, formatter, true));
                product.setPrecioVenta(readDecimal(row, 5, formatter, true));
                product.setPrecioVentaMayorista(readDecimal(row, 6, formatter, false));
                product.setCantidadMayoristaMinima(readDecimal(row, 7, formatter, false));
                product.setStockActual(readDecimal(row, 8, formatter, true));
                product.setStockMinimo(readDecimal(row, 9, formatter, true));
                product.setUnidadMedida(defaultIfBlank(readCell(row, 10, formatter), "UNIDAD"));
                product.setVatType(normalizeVatType(defaultIfBlank(readCell(row, 11, formatter), "IVA_10")));
                product.setCodigoBarras(trimToNull(readCell(row, 12, formatter)));
                product.setActivo(readBoolean(row, 13, formatter));

                validateWholesalePricing(new ProductRequest(
                        product.getCodigo(),
                        product.getNombre(),
                        product.getDescripcion(),
                        product.getCategoria(),
                        product.getPrecioCosto(),
                        product.getPrecioVenta(),
                        product.getPrecioVentaMayorista(),
                        product.getCantidadMayoristaMinima(),
                        product.getStockActual(),
                        product.getStockMinimo(),
                        product.getUnidadMedida(),
                        product.getVatType(),
                        product.getCodigoBarras(),
                        product.isActivo()
                ));

                product = productRepository.save(product);
                product.setQrContenido("http://localhost:4200/productos/" + product.getId() + "/qr-view");
                productRepository.save(product);
                imported++;
            }

            return new ProductImportResponse(
                    imported,
                    skipped,
                    "Importacion finalizada. Productos importados: " + imported + ". Omitidos: " + skipped + "."
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo Excel.", e);
        }
    }

    private void apply(Product product, ProductRequest request) {
        validateWholesalePricing(request);
        product.setCodigo(request.codigo().trim());
        product.setNombre(request.nombre().trim());
        product.setDescripcion(request.descripcion());
        product.setCategoria(request.categoria());
        product.setPrecioCosto(request.precioCosto());
        product.setPrecioVenta(request.precioVenta());
        product.setPrecioVentaMayorista(normalizeOptionalPositive(request.precioVentaMayorista()));
        product.setCantidadMayoristaMinima(normalizeOptionalPositive(request.cantidadMayoristaMinima()));
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
                product.getPrecioVentaMayorista(),
                product.getCantidadMayoristaMinima(),
                product.getStockActual(),
                product.getStockMinimo(),
                product.getUnidadMedida(),
                product.getVatType(),
                product.isActivo(),
                product.getQrContenido(),
                product.getCodigoBarras()
        );
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private BigDecimal readDecimal(Row row, int index, DataFormatter formatter, boolean required) {
        String raw = readCell(row, index, formatter);
        if (raw == null || raw.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("Falta un valor numerico obligatorio en la plantilla.");
            }
            return null;
        }

        try {
            return new java.math.BigDecimal(raw.replace(".", "").replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor numerico invalido en la plantilla: " + raw);
        }
    }

    private boolean readBoolean(Row row, int index, DataFormatter formatter) {
        String raw = readCell(row, index, formatter);
        if (raw == null || raw.isBlank()) {
            return true;
        }
        return "true".equalsIgnoreCase(raw) || "1".equals(raw) || "si".equalsIgnoreCase(raw);
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (int i = 0; i <= 13; i++) {
            if (!readCell(row, i, formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeVatType(String vatType) {
        String normalized = vatType == null ? "" : vatType.trim().toUpperCase();
        return switch (normalized) {
            case "IVA_10", "IVA_5", "EXENTO" -> normalized;
            default -> throw new IllegalArgumentException("Tipo de IVA inv\u00e1lido.");
        };
    }

    private void validateWholesalePricing(ProductRequest request) {
        boolean hasWholesalePrice = request.precioVentaMayorista() != null
                && request.precioVentaMayorista().compareTo(java.math.BigDecimal.ZERO) > 0;
        boolean hasWholesaleQuantity = request.cantidadMayoristaMinima() != null
                && request.cantidadMayoristaMinima().compareTo(java.math.BigDecimal.ZERO) > 0;

        if (!hasWholesalePrice && !hasWholesaleQuantity) {
            return;
        }

        if (!hasWholesalePrice) {
            throw new IllegalArgumentException("Debe completar el precio venta 2 para usar precio mayorista.");
        }

        if (!hasWholesaleQuantity || request.cantidadMayoristaMinima().compareTo(java.math.BigDecimal.valueOf(2)) < 0) {
            throw new IllegalArgumentException("La cantidad mayorista debe ser al menos 2 unidades.");
        }

        if (request.precioVentaMayorista().compareTo(request.precioVenta()) >= 0) {
            throw new IllegalArgumentException("El precio venta 2 debe ser menor al precio de venta principal.");
        }
    }

    private java.math.BigDecimal normalizeOptionalPositive(java.math.BigDecimal value) {
        if (value == null || value.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return null;
        }
        return value;
    }
}

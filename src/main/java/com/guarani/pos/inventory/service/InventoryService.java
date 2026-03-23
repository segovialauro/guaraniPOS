package com.guarani.pos.inventory.service;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.inventory.dto.InventoryAdjustRequest;
import com.guarani.pos.inventory.dto.InventoryItemResponse;
import com.guarani.pos.inventory.dto.InventoryMovementResponse;
import com.guarani.pos.inventory.dto.InventorySummaryResponse;
import com.guarani.pos.inventory.model.InventoryMovement;
import com.guarani.pos.inventory.repository.InventoryMovementRepository;
import com.guarani.pos.inventory.security.InventoryPermission;
import com.guarani.pos.product.model.Product;
import com.guarani.pos.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final AuthorizationService authorizationService;

    public InventoryService(ProductRepository productRepository,
                            CompanyRepository companyRepository,
                            UserRepository userRepository,
                            InventoryMovementRepository inventoryMovementRepository,
                            AuthorizationService authorizationService) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public InventorySummaryResponse getSummary(Long companyId, Long userId) {
        authorizationService.checkPermission(userId, InventoryPermission.INVENTARIO_VER);

        List<Product> products = productRepository.findByCompanyIdOrderByNombreAsc(companyId).stream()
                .filter(Product::isActivo)
                .toList();

        BigDecimal inventoryValue = products.stream()
                .map(product -> safe(product.getPrecioCosto()).multiply(safe(product.getStockActual())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnits = products.stream()
                .map(product -> safe(product.getStockActual()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int lowStockItems = (int) products.stream()
                .filter(this::isLowStock)
                .count();

        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDateTime from = firstDay.atStartOfDay();
        LocalDateTime to = firstDay.plusMonths(1).atStartOfDay();

        long monthlyMovements = inventoryMovementRepository.countByCompanyIdAndCreatedAtBetween(companyId, from, to);

        List<String> categories = products.stream()
                .map(Product::getCategoria)
                .filter(category -> category != null && !category.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        return new InventorySummaryResponse(
                inventoryValue,
                totalUnits,
                products.size(),
                lowStockItems,
                monthlyMovements,
                categories
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> findProducts(Long companyId, Long userId, String q, String category, boolean lowStockOnly) {
        authorizationService.checkPermission(userId, InventoryPermission.INVENTARIO_VER);

        String normalizedQuery = q == null ? "" : q.trim();
        String normalizedCategory = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);

        List<Product> products = normalizedQuery.isBlank()
                ? productRepository.findByCompanyIdOrderByNombreAsc(companyId)
                : productRepository.search(companyId, normalizedQuery);

        return products.stream()
                .filter(Product::isActivo)
                .filter(product -> normalizedCategory.isBlank()
                        || (product.getCategoria() != null
                        && product.getCategoria().trim().toUpperCase(Locale.ROOT).equals(normalizedCategory)))
                .filter(product -> !lowStockOnly || isLowStock(product))
                .map(this::toItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findRecentMovements(Long companyId, Long userId, Long productId) {
        authorizationService.checkPermission(userId, InventoryPermission.INVENTARIO_VER);

        List<InventoryMovement> movements = productId == null
                ? inventoryMovementRepository.findTop20ByCompanyIdOrderByCreatedAtDesc(companyId)
                : inventoryMovementRepository.findTop20ByCompanyIdAndProductIdOrderByCreatedAtDesc(companyId, productId);

        return movements.stream()
                .sorted(Comparator.comparing(InventoryMovement::getCreatedAt).reversed())
                .map(this::toMovementResponse)
                .toList();
    }

    @Transactional
    public InventoryMovementResponse adjustStock(Long companyId, Long userId, InventoryAdjustRequest request) {
        authorizationService.checkPermission(userId, InventoryPermission.INVENTARIO_AJUSTAR);

        companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Product product = productRepository.findByIdAndCompanyId(request.productId(), companyId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        BigDecimal previousStock = safe(product.getStockActual());
        BigDecimal newStock = safe(request.newStock());

        if (previousStock.compareTo(newStock) == 0) {
            throw new IllegalArgumentException("El nuevo stock debe ser diferente al stock actual.");
        }

        BigDecimal quantity = newStock.subtract(previousStock).abs().setScale(2, RoundingMode.HALF_UP);
        String movementType = newStock.compareTo(previousStock) > 0 ? "ENTRADA" : "SALIDA";

        product.setStockActual(newStock);
        productRepository.save(product);

        InventoryMovement movement = new InventoryMovement();
        movement.setCompany(product.getCompany());
        movement.setProduct(product);
        movement.setUser(user);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(request.reason().trim());

        return toMovementResponse(inventoryMovementRepository.save(movement));
    }

    private InventoryItemResponse toItemResponse(Product product) {
        BigDecimal currentStock = safe(product.getStockActual());
        BigDecimal minimumStock = safe(product.getStockMinimo());
        BigDecimal totalValue = safe(product.getPrecioCosto()).multiply(currentStock);

        return new InventoryItemResponse(
                product.getId(),
                product.getCodigo(),
                product.getNombre(),
                product.getCategoria(),
                product.getUnidadMedida(),
                product.getPrecioCosto(),
                product.getPrecioVenta(),
                currentStock,
                minimumStock,
                totalValue,
                product.getVatType(),
                isLowStock(product)
        );
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getCodigo(),
                movement.getProduct().getNombre(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getPreviousStock(),
                movement.getNewStock(),
                movement.getReason(),
                movement.getUser().getFullName(),
                movement.getCreatedAt()
        );
    }

    private boolean isLowStock(Product product) {
        return safe(product.getStockMinimo()).compareTo(BigDecimal.ZERO) > 0
                && safe(product.getStockActual()).compareTo(safe(product.getStockMinimo())) <= 0;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

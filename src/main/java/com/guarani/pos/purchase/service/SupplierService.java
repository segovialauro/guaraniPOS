package com.guarani.pos.purchase.service;

import com.guarani.pos.auth.service.AuthorizationService;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.purchase.repository.PurchaseRepository;
import com.guarani.pos.purchase.security.PurchasePermission;
import com.guarani.pos.supplier.dto.SupplierRequest;
import com.guarani.pos.supplier.dto.SupplierResponse;
import com.guarani.pos.supplier.model.Supplier;
import com.guarani.pos.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;
    private final PurchaseRepository purchaseRepository;
    private final AuthorizationService authorizationService;

    public SupplierService(SupplierRepository supplierRepository,
                           CompanyRepository companyRepository,
                           PurchaseRepository purchaseRepository,
                           AuthorizationService authorizationService) {
        this.supplierRepository = supplierRepository;
        this.companyRepository = companyRepository;
        this.purchaseRepository = purchaseRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll(Long companyId, Long userId, String q) {
        authorizationService.checkPermission(companyId, userId, PurchasePermission.PROVEEDOR_VER);

        List<Supplier> suppliers = (q == null || q.isBlank())
                ? supplierRepository.findByCompanyIdOrderByNameAsc(companyId)
                : supplierRepository.search(companyId, q.trim());

        Map<Long, BigDecimal> payableBalancesBySupplier = loadPayableBalancesBySupplier(companyId);

        return suppliers.stream()
                .map(supplier -> toResponse(supplier, payableBalancesBySupplier))
                .toList();
    }

    @Transactional
    public SupplierResponse create(Long companyId, Long userId, SupplierRequest request) {
        authorizationService.checkPermission(companyId, userId, PurchasePermission.PROVEEDOR_GESTIONAR);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        String ruc = trimToNull(request.ruc());
        if (ruc != null && supplierRepository.existsByCompanyIdAndRucIgnoreCase(companyId, ruc)) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese RUC.");
        }

        Supplier supplier = new Supplier();
        supplier.setCompany(company);
        apply(supplier, request);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return toResponse(savedSupplier, Map.of());
    }

    private void apply(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name().trim());
        supplier.setRuc(trimToNull(request.ruc()));
        supplier.setPhone(trimToNull(request.phone()));
        supplier.setEmail(trimToNull(request.email()));
        supplier.setAddress(trimToNull(request.address()));
        supplier.setObservation(trimToNull(request.observation()));
        supplier.setActive(request.active());
    }

    private SupplierResponse toResponse(Supplier supplier, Map<Long, BigDecimal> payableBalancesBySupplier) {
        BigDecimal payableBalance = payableBalancesBySupplier.getOrDefault(supplier.getId(), BigDecimal.ZERO);

        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getRuc(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getObservation(),
                supplier.isActive(),
                payableBalance
        );
    }

    private Map<Long, BigDecimal> loadPayableBalancesBySupplier(Long companyId) {
        Map<Long, BigDecimal> balances = new HashMap<>();

        for (Object[] row : purchaseRepository.sumPayableBalanceGroupedBySupplier(companyId)) {
            Long supplierId = row[0] instanceof Number number ? number.longValue() : null;
            BigDecimal balance = row[1] instanceof BigDecimal value ? value : BigDecimal.ZERO;
            if (supplierId != null) {
                balances.put(supplierId, balance);
            }
        }

        return balances;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

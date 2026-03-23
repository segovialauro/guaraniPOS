package com.guarani.pos.auth.service;

import static com.guarani.pos.cash.security.CashPermission.CAJA_ABRIR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_CERRAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_MOVIMIENTO_ANULAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_MOVIMIENTO_CREAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_MOVIMIENTO_EDITAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_REPORTE_DESCARGAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_REPORTE_VER;
import static com.guarani.pos.inventory.security.InventoryPermission.INVENTARIO_AJUSTAR;
import static com.guarani.pos.inventory.security.InventoryPermission.INVENTARIO_VER;
import static com.guarani.pos.purchase.security.PurchasePermission.COMPRAS_PAGAR;
import static com.guarani.pos.purchase.security.PurchasePermission.COMPRAS_EDITAR;
import static com.guarani.pos.purchase.security.PurchasePermission.COMPRAS_ANULAR;
import static com.guarani.pos.purchase.security.PurchasePermission.COMPRAS_REGISTRAR;
import static com.guarani.pos.purchase.security.PurchasePermission.COMPRAS_VER;
import static com.guarani.pos.purchase.security.PurchasePermission.PROVEEDOR_GESTIONAR;
import static com.guarani.pos.purchase.security.PurchasePermission.PROVEEDOR_VER;
import static com.guarani.pos.sale.security.SalePermission.VENTA_CREAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_ANULAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_DEVOLVER;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_DESCARGAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_VER;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.shared.exception.ForbiddenOperationException;

@Service
public class AuthorizationService {

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<String> getPermissionsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        return getPermissionsByRoleCode(user.getRoleCode());
    }

    public boolean hasPermission(Long userId, String permissionCode) {
        return getPermissionsByUserId(userId).contains(permissionCode);
    }

    public void checkPermission(Long userId, String permissionCode) {
        if (!hasPermission(userId, permissionCode)) {
            throw new ForbiddenOperationException("No tienes permisos para realizar esta accion.");
        }
    }

    private Set<String> getPermissionsByRoleCode(String roleCode) {
        String role = normalizeRole(roleCode);
        Set<String> permissions = new LinkedHashSet<>();

        switch (role) {
            case "ADMIN_EMPRESA" -> {
                permissions.add(INVENTARIO_VER);
                permissions.add(INVENTARIO_AJUSTAR);
                permissions.add(COMPRAS_VER);
                permissions.add(COMPRAS_REGISTRAR);
                permissions.add(COMPRAS_EDITAR);
                permissions.add(COMPRAS_ANULAR);
                permissions.add(COMPRAS_PAGAR);
                permissions.add(PROVEEDOR_VER);
                permissions.add(PROVEEDOR_GESTIONAR);
                permissions.add(CAJA_ABRIR);
                permissions.add(CAJA_CERRAR);
                permissions.add(CAJA_MOVIMIENTO_CREAR);
                permissions.add(CAJA_MOVIMIENTO_EDITAR);
                permissions.add(CAJA_MOVIMIENTO_ANULAR);
                permissions.add(CAJA_REPORTE_VER);
                permissions.add(CAJA_REPORTE_DESCARGAR);
                permissions.add(VENTA_CREAR);
                permissions.add(VENTA_ANULAR);
                permissions.add(VENTA_DEVOLVER);
                permissions.add(VENTA_TICKET_VER);
                permissions.add(VENTA_TICKET_DESCARGAR);
            }
            case "SUPERVISOR" -> {
                permissions.add(INVENTARIO_VER);
                permissions.add(INVENTARIO_AJUSTAR);
                permissions.add(COMPRAS_VER);
                permissions.add(COMPRAS_REGISTRAR);
                permissions.add(COMPRAS_EDITAR);
                permissions.add(COMPRAS_ANULAR);
                permissions.add(COMPRAS_PAGAR);
                permissions.add(PROVEEDOR_VER);
                permissions.add(PROVEEDOR_GESTIONAR);
                permissions.add(CAJA_ABRIR);
                permissions.add(CAJA_CERRAR);
                permissions.add(CAJA_MOVIMIENTO_CREAR);
                permissions.add(CAJA_MOVIMIENTO_EDITAR);
                permissions.add(CAJA_MOVIMIENTO_ANULAR);
                permissions.add(CAJA_REPORTE_VER);
                permissions.add(CAJA_REPORTE_DESCARGAR);
                permissions.add(VENTA_CREAR);
                permissions.add(VENTA_ANULAR);
                permissions.add(VENTA_DEVOLVER);
                permissions.add(VENTA_TICKET_VER);
                permissions.add(VENTA_TICKET_DESCARGAR);
            }
            case "CAJERO" -> {
                permissions.add(INVENTARIO_VER);
                permissions.add(PROVEEDOR_VER);
                permissions.add(CAJA_ABRIR);
                permissions.add(CAJA_CERRAR);
                permissions.add(CAJA_MOVIMIENTO_CREAR);
                permissions.add(CAJA_REPORTE_VER);
                permissions.add(CAJA_REPORTE_DESCARGAR);
                permissions.add(VENTA_CREAR);
                permissions.add(VENTA_TICKET_VER);
                permissions.add(VENTA_TICKET_DESCARGAR);
            }
            default -> {
                // sin permisos
            }
        }

        return permissions;
    }

    private String normalizeRole(String roleCode) {
        String normalized = roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ADMIN")) {
            return "ADMIN_EMPRESA";
        }
        if (normalized.startsWith("SUPERVISOR")) {
            return "SUPERVISOR";
        }
        if (normalized.startsWith("CAJERO")) {
            return "CAJERO";
        }

        return normalized;
    }
}

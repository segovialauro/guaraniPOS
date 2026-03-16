package com.guarani.pos.auth.service;

import static com.guarani.pos.cash.security.CashPermission.CAJA_ABRIR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_CERRAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_MOVIMIENTO_ANULAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_MOVIMIENTO_CREAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_MOVIMIENTO_EDITAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_REPORTE_DESCARGAR;
import static com.guarani.pos.cash.security.CashPermission.CAJA_REPORTE_VER;
import static com.guarani.pos.sale.security.SalePermission.VENTA_CREAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_DESCARGAR;
import static com.guarani.pos.sale.security.SalePermission.VENTA_TICKET_VER;

import java.util.LinkedHashSet;
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
			throw new ForbiddenOperationException("No tienes permisos para realizar esta acción.");
		}
	}

	private Set<String> getPermissionsByRoleCode(String roleCode) {
		String role = roleCode == null ? "" : roleCode.trim().toUpperCase();

		Set<String> permissions = new LinkedHashSet<>();

		switch (role) {
		case "ADMIN_EMPRESA" -> {
			permissions.add(CAJA_ABRIR);
			permissions.add(CAJA_CERRAR);
			permissions.add(CAJA_MOVIMIENTO_CREAR);
			permissions.add(CAJA_MOVIMIENTO_EDITAR);
			permissions.add(CAJA_MOVIMIENTO_ANULAR);
			permissions.add(CAJA_REPORTE_VER);
			permissions.add(CAJA_REPORTE_DESCARGAR);
			permissions.add(VENTA_CREAR);
			permissions.add(VENTA_TICKET_VER);
			permissions.add(VENTA_TICKET_DESCARGAR);
		}
		case "SUPERVISOR" -> {
			permissions.add(CAJA_ABRIR);
			permissions.add(CAJA_CERRAR);
			permissions.add(CAJA_MOVIMIENTO_CREAR);
			permissions.add(CAJA_MOVIMIENTO_EDITAR);
			permissions.add(CAJA_MOVIMIENTO_ANULAR);
			permissions.add(CAJA_REPORTE_VER);
			permissions.add(CAJA_REPORTE_DESCARGAR);
			permissions.add(VENTA_CREAR);
			permissions.add(VENTA_TICKET_VER);
			permissions.add(VENTA_TICKET_DESCARGAR);
		}
		case "CAJERO" -> {
			permissions.add(CAJA_ABRIR);
			permissions.add(CAJA_CERRAR);
			permissions.add(CAJA_MOVIMIENTO_CREAR);
			permissions.add(CAJA_REPORTE_VER);
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
}

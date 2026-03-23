package com.guarani.pos.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static Long getCurrentCompanyId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new IllegalStateException("No hay autenticación.");
		}

		Object details = authentication.getDetails();
		if (details instanceof JwtUserDetails jwtUserDetails) {
			return jwtUserDetails.companyId();
		}

		throw new IllegalStateException("No se pudo obtener la empresa del token.");
	}

	public static Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new IllegalStateException("No hay autenticación");
		}

		Object details = authentication.getDetails();

		if (details instanceof JwtUserDetails jwtUserDetails) {
			return jwtUserDetails.userId();
		}

		throw new IllegalStateException("No se pudo obtener userId del token");
	}

	public static String getCurrentRole() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new IllegalStateException("No hay autenticacion");
		}

		Object details = authentication.getDetails();

		if (details instanceof JwtUserDetails jwtUserDetails) {
			return jwtUserDetails.role();
		}

		throw new IllegalStateException("No se pudo obtener el rol del token");
	}
}

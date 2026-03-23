package com.guarani.pos.auth.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.dto.UserAdminRequest;
import com.guarani.pos.auth.dto.UserAdminResponse;
import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository,
                            CompanyRepository companyRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> findAll(Long companyId) {
        return userRepository.findByCompanyIdOrderByFullNameAsc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserAdminResponse create(Long companyId, UserAdminRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        String cedula = normalizeRequired(request.cedula(), "La cedula es obligatoria.");
        String roleCode = normalizeRole(request.roleCode());
        String quickPin = normalizeQuickPin(request.quickPin());

        validateUnique(companyId, cedula, quickPin, null);

        User user = new User();
        user.setCompany(company);
        user.setCedula(cedula);
        user.setFullName(normalizeRequired(request.fullName(), "El nombre es obligatorio."));
        user.setPasswordHash(passwordEncoder.encode(request.password().trim()));
        user.setQuickPin(quickPin);
        user.setRoleCode(roleCode);
        user.setStatus(request.active() ? "ACTIVO" : "INACTIVO");

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changeStatus(Long companyId, Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!user.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("No se puede modificar un usuario de otra empresa.");
        }

        user.setStatus(active ? "ACTIVO" : "INACTIVO");
        userRepository.save(user);
    }

    private void validateUnique(Long companyId, String cedula, String quickPin, Long currentId) {
        boolean cedulaExists = currentId == null
                ? userRepository.existsByCompanyIdAndCedulaIgnoreCase(companyId, cedula)
                : userRepository.existsByCompanyIdAndCedulaIgnoreCaseAndIdNot(companyId, cedula, currentId);

        if (cedulaExists) {
            throw new IllegalArgumentException("Ya existe un usuario con esa cedula.");
        }

        if (quickPin != null) {
            boolean pinExists = currentId == null
                    ? userRepository.existsByCompanyIdAndQuickPin(companyId, quickPin)
                    : userRepository.existsByCompanyIdAndQuickPinAndIdNot(companyId, quickPin, currentId);

            if (pinExists) {
                throw new IllegalArgumentException("Ya existe un usuario con ese PIN.");
            }
        }
    }

    private String normalizeRole(String roleCode) {
        String normalized = normalizeRequired(roleCode, "El rol es obligatorio.").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CAJERO", "SUPERVISOR" -> normalized;
            default -> throw new IllegalArgumentException("Solo se permite crear usuarios CAJERO o SUPERVISOR.");
        };
    }

    private String normalizeQuickPin(String quickPin) {
        if (quickPin == null || quickPin.trim().isEmpty()) {
            return null;
        }

        String normalized = quickPin.trim();
        if (!normalized.matches("\\d{4}")) {
            throw new IllegalArgumentException("El PIN debe tener exactamente 4 digitos.");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private UserAdminResponse toResponse(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getCedula(),
                user.getFullName(),
                user.getQuickPin(),
                user.getRoleCode(),
                user.getStatus());
    }
}

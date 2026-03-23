package com.guarani.pos.auth.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.dto.LoginRequest;
import com.guarani.pos.auth.dto.LoginResponse;
import com.guarani.pos.auth.dto.QuickPinRequest;
import com.guarani.pos.auth.model.User;
import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CompanyRepository companyRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String tenantCode = normalizeRequired(request.tenantCode(), "El codigo de empresa es obligatorio.");
        String cedula = normalizeRequired(request.cedula(), "La cedula es obligatoria.");
        String password = normalizeRequired(request.password(), "La contrasena es obligatoria.");

        Company company = resolveAndValidateCompany(tenantCode);
        User user = userRepository.findByCompanyIdAndCedula(company.getId(), cedula)
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contrasena invalidos."));

        validateActiveUser(user);

        if (!matchesPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Usuario o contrasena invalidos.");
        }

        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse quickPin(QuickPinRequest request) {
        String tenantCode = normalizeRequired(request.tenantCode(), "El codigo de empresa es obligatorio.");
        String pin = normalizeRequired(request.pin(), "El PIN es obligatorio.");

        Company company = resolveAndValidateCompany(tenantCode);
        User user = userRepository.findByCompanyIdAndQuickPin(company.getId(), pin)
                .orElseThrow(() -> new IllegalArgumentException("PIN invalido."));

        validateActiveUser(user);
        return buildResponse(user);
    }

    private Company resolveAndValidateCompany(String tenantCode) {
        Company company = companyRepository.findByCodeIgnoreCase(tenantCode)
                .orElseThrow(() -> new IllegalArgumentException("Empresa o licencia no valida."));

        if (!"ACTIVA".equalsIgnoreCase(company.getStatus())) {
            throw new IllegalArgumentException("La empresa se encuentra suspendida o inactiva.");
        }

        if (!"ACTIVA".equalsIgnoreCase(company.getLicenseStatus())) {
            throw new IllegalArgumentException("La licencia no esta activa.");
        }

        if (company.getLicenseDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La licencia esta vencida.");
        }

        return company;
    }

    private void validateActiveUser(User user) {
        if (!"ACTIVO".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("El usuario esta inactivo.");
        }
    }

    private LoginResponse buildResponse(User user) {
        return new LoginResponse(
                jwtService.generateToken(user),
                user.getCompany().getCode(),
                user.getCompany().getName(),
                user.getId(),
                user.getFullName(),
                user.getRoleCode()
        );
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

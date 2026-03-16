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
        Company company = resolveAndValidateCompany(request.tenantCode());
        User user = userRepository.findByCompanyIdAndCedula(company.getId(), request.cedula())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña inválidos."));

        validateActiveUser(user);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Usuario o contraseña inválidos.");
        }

        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse quickPin(QuickPinRequest request) {
        Company company = resolveAndValidateCompany(request.tenantCode());
        User user = userRepository.findByCompanyIdAndQuickPin(company.getId(), request.pin())
                .orElseThrow(() -> new IllegalArgumentException("PIN inválido."));

        validateActiveUser(user);
        return buildResponse(user);
    }

    private Company resolveAndValidateCompany(String tenantCode) {
        Company company = companyRepository.findByCodeIgnoreCase(tenantCode)
                .orElseThrow(() -> new IllegalArgumentException("Empresa o licencia no válida."));

        if (!"ACTIVA".equalsIgnoreCase(company.getStatus())) {
            throw new IllegalArgumentException("La empresa se encuentra suspendida o inactiva.");
        }

        if (!"ACTIVA".equalsIgnoreCase(company.getLicenseStatus())) {
            throw new IllegalArgumentException("La licencia no está activa.");
        }

        if (company.getLicenseDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La licencia está vencida.");
        }

        return company;
    }

    private void validateActiveUser(User user) {
        if (!"ACTIVO".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("El usuario está inactivo.");
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
}

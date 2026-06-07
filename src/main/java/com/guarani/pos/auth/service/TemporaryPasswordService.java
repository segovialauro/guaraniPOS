package com.guarani.pos.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.auth.model.User;
import com.guarani.pos.common.parameter.model.AppParameter;
import com.guarani.pos.common.parameter.repository.AppParameterRepository;
import com.guarani.pos.company.model.Company;

@Service
public class TemporaryPasswordService {

    private static final String GROUP_CODE = "SECURITY_ACCESS";
    private static final String CODE_PREFIX = "FORCE_PASSWORD_";

    private final AppParameterRepository appParameterRepository;

    public TemporaryPasswordService(AppParameterRepository appParameterRepository) {
        this.appParameterRepository = appParameterRepository;
    }

    @Transactional
    public void markRequired(Company company, User user) {
        String code = buildCode(user.getId());
        AppParameter parameter = appParameterRepository
                .findByCompanyIdAndGroupCodeAndCodeIgnoreCase(company.getId(), GROUP_CODE, code)
                .orElseGet(AppParameter::new);

        parameter.setCompany(company);
        parameter.setGroupCode(GROUP_CODE);
        parameter.setCode(code);
        parameter.setLabel("Forzar cambio de contraseña");
        parameter.setDescription("Obliga al usuario a cambiar su contraseña temporal.");
        parameter.setSortOrder(0);
        parameter.setSystemDefined(true);
        parameter.setActive(true);
        appParameterRepository.save(parameter);
    }

    @Transactional(readOnly = true)
    public boolean isRequired(Long companyId, Long userId) {
        return appParameterRepository
                .findByCompanyIdAndGroupCodeAndCodeIgnoreCase(companyId, GROUP_CODE, buildCode(userId))
                .map(AppParameter::isActive)
                .orElse(false);
    }

    @Transactional
    public void clearRequired(Long companyId, Long userId) {
        appParameterRepository
                .findByCompanyIdAndGroupCodeAndCodeIgnoreCase(companyId, GROUP_CODE, buildCode(userId))
                .ifPresent(parameter -> {
                    parameter.setActive(false);
                    appParameterRepository.save(parameter);
                });
    }

    private String buildCode(Long userId) {
        return CODE_PREFIX + userId;
    }
}

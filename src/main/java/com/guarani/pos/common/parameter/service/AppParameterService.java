package com.guarani.pos.common.parameter.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.common.parameter.dto.AppParameterRequest;
import com.guarani.pos.common.parameter.dto.AppParameterResponse;
import com.guarani.pos.common.parameter.model.AppParameter;
import com.guarani.pos.common.parameter.repository.AppParameterRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;

@Service
public class AppParameterService {

    private final AppParameterRepository appParameterRepository;
    private final CompanyRepository companyRepository;

    public AppParameterService(AppParameterRepository appParameterRepository,
                               CompanyRepository companyRepository) {
        this.appParameterRepository = appParameterRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<AppParameterResponse> findByGroupCode(Long companyId, String groupCode) {
        String normalizedGroupCode = normalizeGroupCode(groupCode);

        List<AppParameter> globalParameters =
                appParameterRepository.findByActiveTrueAndGroupCodeAndCompanyIsNullOrderBySortOrderAscLabelAsc(
                        normalizedGroupCode);
        List<AppParameter> companyParameters =
                appParameterRepository.findByActiveTrueAndGroupCodeAndCompanyIdOrderBySortOrderAscLabelAsc(
                        normalizedGroupCode, companyId);

        Map<String, AppParameter> merged = new LinkedHashMap<>();
        for (AppParameter parameter : globalParameters) {
            merged.put(parameter.getCode(), parameter);
        }
        for (AppParameter parameter : companyParameters) {
            merged.put(parameter.getCode(), parameter);
        }

        List<AppParameterResponse> response = new ArrayList<>();
        for (AppParameter parameter : merged.values()) {
            response.add(new AppParameterResponse(
                    parameter.getId(),
                    parameter.getGroupCode(),
                    parameter.getCode(),
                    parameter.getLabel(),
                    parameter.getDescription(),
                    parameter.getSortOrder(),
                    parameter.isActive(),
                    parameter.isSystemDefined()));
        }
        return response;
    }

    @Transactional
    public AppParameterResponse create(Long companyId, AppParameterRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        String normalizedGroupCode = normalizeGroupCode(request.groupCode());
        String normalizedCode = normalizeCode(request.code());

        if (appParameterRepository.existsByCompanyIdAndGroupCodeAndCodeIgnoreCase(
                companyId, normalizedGroupCode, normalizedCode)) {
            throw new IllegalArgumentException("Ya existe un parametro con ese codigo dentro del grupo.");
        }

        AppParameter parameter = new AppParameter();
        parameter.setCompany(company);
        apply(parameter, request, normalizedGroupCode, normalizedCode);
        parameter.setSystemDefined(false);

        return toResponse(appParameterRepository.save(parameter));
    }

    @Transactional
    public AppParameterResponse update(Long companyId, Long id, AppParameterRequest request) {
        AppParameter parameter = appParameterRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Parametro no encontrado."));

        if (parameter.isSystemDefined()) {
            throw new IllegalArgumentException("Los parametros base del sistema no se editan desde esta pantalla.");
        }

        String normalizedGroupCode = normalizeGroupCode(request.groupCode());
        String normalizedCode = normalizeCode(request.code());

        if (appParameterRepository.existsByCompanyIdAndGroupCodeAndCodeIgnoreCaseAndIdNot(
                companyId, normalizedGroupCode, normalizedCode, id)) {
            throw new IllegalArgumentException("Ya existe otro parametro con ese codigo dentro del grupo.");
        }

        apply(parameter, request, normalizedGroupCode, normalizedCode);
        return toResponse(appParameterRepository.save(parameter));
    }

    @Transactional
    public void changeStatus(Long companyId, Long id, boolean active) {
        AppParameter parameter = appParameterRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Parametro no encontrado."));

        if (parameter.isSystemDefined()) {
            throw new IllegalArgumentException("Los parametros base del sistema no se modifican desde esta pantalla.");
        }

        parameter.setActive(active);
        appParameterRepository.save(parameter);
    }

    private void apply(AppParameter parameter,
                       AppParameterRequest request,
                       String normalizedGroupCode,
                       String normalizedCode) {
        parameter.setGroupCode(normalizedGroupCode);
        parameter.setCode(normalizedCode);
        parameter.setLabel(request.label().trim());
        parameter.setDescription(trimToNull(request.description()));
        parameter.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        parameter.setActive(request.active());
    }

    private String normalizeGroupCode(String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            throw new IllegalArgumentException("El grupo parametrico es obligatorio.");
        }
        return groupCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio.");
        }
        return code.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AppParameterResponse toResponse(AppParameter parameter) {
        return new AppParameterResponse(
                parameter.getId(),
                parameter.getGroupCode(),
                parameter.getCode(),
                parameter.getLabel(),
                parameter.getDescription(),
                parameter.getSortOrder(),
                parameter.isActive(),
                parameter.isSystemDefined());
    }
}

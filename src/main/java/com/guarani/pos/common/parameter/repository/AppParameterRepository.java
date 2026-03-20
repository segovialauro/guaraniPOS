package com.guarani.pos.common.parameter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.common.parameter.model.AppParameter;

public interface AppParameterRepository extends JpaRepository<AppParameter, Long> {

    List<AppParameter> findByActiveTrueAndGroupCodeAndCompanyIdOrderBySortOrderAscLabelAsc(String groupCode, Long companyId);

    List<AppParameter> findByActiveTrueAndGroupCodeAndCompanyIsNullOrderBySortOrderAscLabelAsc(String groupCode);

    List<AppParameter> findByGroupCodeAndCompanyIdOrderBySortOrderAscLabelAsc(String groupCode, Long companyId);

    List<AppParameter> findByGroupCodeAndCompanyIsNullOrderBySortOrderAscLabelAsc(String groupCode);

    Optional<AppParameter> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndGroupCodeAndCodeIgnoreCase(Long companyId, String groupCode, String code);

    boolean existsByCompanyIdAndGroupCodeAndCodeIgnoreCaseAndIdNot(Long companyId, String groupCode, String code, Long id);
}

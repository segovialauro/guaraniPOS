package com.guarani.pos.company.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.company.model.CompanyLicenseHistory;

public interface CompanyLicenseHistoryRepository extends JpaRepository<CompanyLicenseHistory, Long> {

    List<CompanyLicenseHistory> findTop10ByCompany_IdOrderByChangedAtDesc(Long companyId);
}

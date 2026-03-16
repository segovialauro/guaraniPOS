package com.guarani.pos.company.repository;

import com.guarani.pos.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCodeIgnoreCase(String code);
}

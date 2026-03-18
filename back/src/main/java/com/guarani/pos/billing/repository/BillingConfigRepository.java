package com.guarani.pos.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.billing.model.BillingConfig;

public interface BillingConfigRepository extends JpaRepository<BillingConfig, Long> {

    Optional<BillingConfig> findFirstByCompany_IdOrderByIdDesc(Long companyId);
}
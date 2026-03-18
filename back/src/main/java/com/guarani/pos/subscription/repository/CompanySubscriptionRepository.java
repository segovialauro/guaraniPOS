package com.guarani.pos.subscription.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.subscription.model.CompanySubscription;

public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Long> {

	Optional<CompanySubscription> findFirstByCompany_IdAndStatusOrderByStartDateDesc(Long companyId, String status);

	Optional<CompanySubscription> findByCompany_IdAndStatus(Long companyId, String status);
}
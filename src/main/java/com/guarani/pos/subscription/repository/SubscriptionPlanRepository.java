package com.guarani.pos.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.subscription.model.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCodeIgnoreCase(String code);

    List<SubscriptionPlan> findAllByActiveTrueOrderByPriceMonthlyAsc();
}
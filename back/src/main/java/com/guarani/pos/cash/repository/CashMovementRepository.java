package com.guarani.pos.cash.repository;

import com.guarani.pos.cash.model.CashMovement;
import com.guarani.pos.cash.model.CashMovementStatus;
import com.guarani.pos.cash.model.CashMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {

    List<CashMovement> findAllByCashSession_IdOrderByCreatedAtDesc(Long cashSessionId);

    Optional<CashMovement> findByIdAndCompany_Id(Long id, Long companyId);

    @Query("""
        select coalesce(sum(m.amount), 0)
        from CashMovement m
        where m.cashSession.id = :cashSessionId
          and m.type = :type
          and m.status = :status
    """)
    BigDecimal sumByCashSessionAndTypeAndStatus(Long cashSessionId, CashMovementType type, CashMovementStatus status);
}

package com.guarani.pos.cash.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.cash.model.CashSession;

public interface CashSessionRepository extends JpaRepository<CashSession, Long> {

    Optional<CashSession> findFirstByCompany_IdAndEstadoOrderByOpenedAtDesc(Long companyId, String estado);

    List<CashSession> findAllByCompany_IdOrderByOpenedAtDesc(Long companyId);

    List<CashSession> findAllByCompany_IdAndEstadoOrderByOpenedAtDesc(Long companyId, String estado);

    List<CashSession> findAllByCompany_IdAndOpenedAtBetweenOrderByOpenedAtDesc(
            Long companyId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<CashSession> findAllByCompany_IdAndEstadoAndOpenedAtBetweenOrderByOpenedAtDesc(
            Long companyId,
            String estado,
            LocalDateTime from,
            LocalDateTime to
    );
}

package com.guarani.pos.inventory.repository;

import com.guarani.pos.inventory.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findTop20ByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<InventoryMovement> findTop20ByCompanyIdAndProductIdOrderByCreatedAtDesc(Long companyId, Long productId);

    long countByCompanyIdAndCreatedAtBetween(Long companyId, LocalDateTime from, LocalDateTime to);
}

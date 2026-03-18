package com.guarani.pos.budget.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guarani.pos.budget.model.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findTop20ByCompanyIdOrderByFechaDesc(Long companyId);

    long countByCompanyId(Long companyId);

    @Query("""
        select count(b)
        from Budget b
        where b.company.id = :companyId
          and b.estado = 'PENDIENTE'
    """)
    long countPending(Long companyId);
    
    Optional<Budget> findByIdAndCompanyId(Long id, Long companyId);
}

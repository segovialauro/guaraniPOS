package com.guarani.pos.product.repository;

import com.guarani.pos.product.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("""
            select count(s)
            from Stock s
            where s.company.id = :companyId
              and s.currentStock <= s.minStock
            """)
    long countLowStock(Long companyId);
}

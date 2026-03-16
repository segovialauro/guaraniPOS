package com.guarani.pos.sale.repository;

import com.guarani.pos.sale.model.SalePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SalePaymentRepository extends JpaRepository<SalePayment, Long> {

    @Query("""
        select coalesce(sum(p.amount), 0)
        from SalePayment p
        where p.sale.company.id = :companyId
          and p.sale.fecha >= :startDateTime
          and p.sale.fecha < :endDateTime
          and p.sale.estado = 'CONFIRMADA'
          and p.method = :paymentMethod
    """)
    BigDecimal sumByCompanyPeriodAndPaymentMethod(Long companyId,
                                                  LocalDateTime startDateTime,
                                                  LocalDateTime endDateTime,
                                                  String paymentMethod);
}

package com.guarani.pos.purchase.repository;

import com.guarani.pos.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findTop50ByCompanyIdOrderByPurchaseDateDescCreatedAtDesc(Long companyId);

    Optional<Purchase> findByIdAndCompanyId(Long id, Long companyId);

    @Query("""
            select p
            from Purchase p
            join p.supplier s
            where p.company.id = :companyId
              and (:status = '' or upper(p.status) = :status)
              and (:fromDate is null or p.purchaseDate >= :fromDate)
              and (:toDate is null or p.purchaseDate <= :toDate)
              and (:q = '' or upper(p.invoiceNumber) like :q or upper(s.name) like :q or upper(coalesce(s.ruc, '')) like :q)
            order by p.purchaseDate desc, p.createdAt desc
            """)
    List<Purchase> search(Long companyId, String status, LocalDate fromDate, LocalDate toDate, String q);

    @Query("""
            select coalesce(sum(p.total), 0)
            from Purchase p
            where p.company.id = :companyId
              and p.purchaseDate between :fromDate and :toDate
            """)
    java.math.BigDecimal sumTotalByDateRange(Long companyId, LocalDate fromDate, LocalDate toDate);

    @Query("""
            select coalesce(sum(p.balance), 0)
            from Purchase p
            where p.company.id = :companyId
              and p.balance > 0
            """)
    java.math.BigDecimal sumPayableBalance(Long companyId);

    long countByCompanyIdAndBalanceGreaterThan(Long companyId, java.math.BigDecimal amount);

    long countByCompanyIdAndCreatedAtBetween(Long companyId, LocalDateTime from, LocalDateTime to);

    boolean existsByCompanyIdAndSupplierIdAndInvoiceNumberIgnoreCase(Long companyId, Long supplierId, String invoiceNumber);

    boolean existsByCompanyIdAndSupplierIdAndInvoiceNumberIgnoreCaseAndIdNot(Long companyId, Long supplierId, String invoiceNumber, Long id);
}

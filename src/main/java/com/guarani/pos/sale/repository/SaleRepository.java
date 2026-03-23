package com.guarani.pos.sale.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import com.guarani.pos.sale.model.Sale;
import com.guarani.pos.sale.dto.SalesOperationalCashierSummaryResponse;

public interface SaleRepository extends JpaRepository<Sale, Long> {

	List<Sale> findTop20ByCompanyIdOrderByFechaDesc(Long companyId);

	long countByCompanyId(Long companyId);

	@Query("""
			    select count(s)
			    from Sale s
			    where s.company.id = :companyId
			      and s.fecha >= :startDateTime
			      and s.fecha < :endDateTime
			      and s.estado = 'CONFIRMADA'
			""")
	long countByCompanyAndPeriod(Long companyId, LocalDateTime startDateTime, LocalDateTime endDateTime);

	@Query("""
			    select coalesce(sum(s.total), 0)
			    from Sale s
			    where s.company.id = :companyId
			      and s.fecha >= :startDateTime
			      and s.fecha < :endDateTime
			      and s.estado = 'CONFIRMADA'
			""")
	BigDecimal sumByCompanyAndPeriod(Long companyId, LocalDateTime startDateTime, LocalDateTime endDateTime);

	@Query("""
			    select coalesce(sum(s.total), 0)
			    from Sale s
			    where s.company.id = :companyId
			      and s.fecha >= :startDateTime
			      and s.fecha < :endDateTime
			      and s.estado = 'CONFIRMADA'
			      and s.metodoPago = :paymentMethod
			""")
	BigDecimal sumByCompanyPeriodAndPaymentMethod(Long companyId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, String paymentMethod);

	@Query("""
			    select coalesce(sum(s.total), 0)
			    from Sale s
			    where s.company.id = :companyId
			      and s.fecha >= :startDateTime
			      and s.fecha < :endDateTime
			      and s.estado = 'CONFIRMADA'
			""")
	BigDecimal sumByCompanyAndDateTimePeriod(Long companyId, LocalDateTime startDateTime,
			LocalDateTime endDateTime);
	
	Optional<Sale> findByIdAndCompany_Id(Long id, Long companyId);

	@Query("""
			select s
			from Sale s
			left join s.customer c
			where s.company.id = :companyId
			  and s.fecha >= :fromDate
			  and s.fecha < :toDate
			  and (:status = '' or s.estado = :status)
			  and (
			    :queryPattern = ''
			    or upper(s.numeroOperacion) like :queryPattern
			    or upper(coalesce(c.nombre, '')) like :queryPattern
			  )
			order by s.fecha desc
			""")
	List<Sale> findAuditHistory(Long companyId,
			LocalDateTime fromDate,
			LocalDateTime toDate,
			String status,
			String queryPattern,
			Pageable pageable);

	@Query("""
			select new com.guarani.pos.sale.dto.SalesOperationalCashierSummaryResponse(
				u.id,
				coalesce(u.fullName, 'Sin usuario'),
				sum(case when s.estado = 'CONFIRMADA' then 1 else 0 end),
				coalesce(sum(case when s.estado = 'CONFIRMADA' then s.total else 0 end), 0),
				sum(case when s.estado = 'ANULADA' then 1 else 0 end),
				coalesce(sum(case when s.estado = 'ANULADA' then s.subtotal else 0 end), 0),
				sum(case when s.estado = 'DEVUELTA_PARCIAL' then 1 else 0 end),
				coalesce(sum(case when s.estado = 'DEVUELTA_PARCIAL' then s.returnTotal else 0 end), 0),
				sum(case when s.estado = 'DEVUELTA_TOTAL' then 1 else 0 end),
				coalesce(sum(case when s.estado = 'DEVUELTA_TOTAL' then s.returnTotal else 0 end), 0)
			)
			from Sale s
			left join s.createdBy u
			where s.company.id = :companyId
			  and s.fecha >= :fromDate
			  and s.fecha < :toDate
			group by u.id, u.fullName
			order by u.fullName asc
			""")
	List<SalesOperationalCashierSummaryResponse> summarizeOperationalByCashier(
			Long companyId,
			LocalDateTime fromDate,
			LocalDateTime toDate);

}

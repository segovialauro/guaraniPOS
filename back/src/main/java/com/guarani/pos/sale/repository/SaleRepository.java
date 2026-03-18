package com.guarani.pos.sale.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guarani.pos.sale.model.Sale;

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

}
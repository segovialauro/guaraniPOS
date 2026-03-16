package com.guarani.pos.pagare.repository;

import com.guarani.pos.pagare.model.Pagare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagareRepository extends JpaRepository<Pagare, Long> {

	List<Pagare> findTop20ByCompanyIdOrderByFechaEmisionDesc(Long companyId);

	Optional<Pagare> findByIdAndCompanyId(Long id, Long companyId);

	long countByCompanyId(Long companyId);

	@Query("""
			    select count(p)
			    from Pagare p
			    where p.company.id = :companyId
			      and p.fechaVencimiento < :today
			      and p.saldo > 0
			      and p.estado <> 'PAGADO'
			      and p.estado <> 'ANULADO'
			""")
	long countOverdue(Long companyId, LocalDate today);

}

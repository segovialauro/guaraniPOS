package com.guarani.pos.supplier.repository;

import com.guarani.pos.supplier.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByCompanyIdOrderByNameAsc(Long companyId);

    Optional<Supplier> findByIdAndCompanyId(Long id, Long companyId);

    @Query("""
            select s
            from Supplier s
            where s.company.id = :companyId
              and (
                lower(s.name) like lower(concat('%', :q, '%'))
                or lower(coalesce(s.ruc, '')) like lower(concat('%', :q, '%'))
              )
            order by s.name asc
            """)
    List<Supplier> search(Long companyId, String q);
}

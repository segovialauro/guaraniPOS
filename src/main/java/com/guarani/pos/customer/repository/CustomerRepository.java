package com.guarani.pos.customer.repository;

import com.guarani.pos.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByCompanyIdOrderByNombreAsc(Long companyId);

    Optional<Customer> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndDocumentoIgnoreCase(Long companyId, String documento);

    boolean existsByCompanyIdAndDocumentoIgnoreCaseAndIdNot(Long companyId, String documento, Long id);

    boolean existsByCompanyIdAndRucIgnoreCase(Long companyId, String ruc);

    boolean existsByCompanyIdAndRucIgnoreCaseAndIdNot(Long companyId, String ruc, Long id);

    @Query("""
        select c
        from Customer c
        where c.company.id = :companyId
          and (
            lower(c.nombre) like lower(concat('%', :q, '%'))
            or lower(coalesce(c.documento, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(c.ruc, '')) like lower(concat('%', :q, '%'))
            or lower(coalesce(c.telefono, '')) like lower(concat('%', :q, '%'))
          )
        order by c.nombre asc
    """)
    List<Customer> search(Long companyId, String q);
}

package com.guarani.pos.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guarani.pos.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findByCompanyIdOrderByNombreAsc(Long companyId);

	List<Product> findByCompanyIdAndNombreContainingIgnoreCaseOrderByNombreAsc(Long companyId, String nombre);

	Optional<Product> findByIdAndCompanyId(Long id, Long companyId);

	boolean existsByCompanyIdAndCodigoIgnoreCase(Long companyId, String codigo);

	boolean existsByCompanyIdAndCodigoIgnoreCaseAndIdNot(Long companyId, String codigo, Long id);

	@Query("""
			    select p
			    from Product p
			    where p.company.id = :companyId
			      and (
			        lower(p.nombre) like lower(concat('%', :q, '%'))
			        or lower(p.codigo) like lower(concat('%', :q, '%'))
			      )
			    order by p.nombre asc
			""")
	List<Product> search(Long companyId, String q);
	
	Optional<Product> findByCompanyIdAndCodigoBarras(Long companyId, String codigoBarras);

}
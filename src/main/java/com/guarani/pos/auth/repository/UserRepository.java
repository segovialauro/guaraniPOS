package com.guarani.pos.auth.repository;

import com.guarani.pos.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("""
			select u
			from User u
			join fetch u.company c
			where c.id = :companyId
			  and u.cedula = :cedula
			""")
	Optional<User> findByCompanyIdAndCedula(Long companyId, String cedula);

	@Query("""
			select u
			from User u
			join fetch u.company c
			where c.id = :companyId
			  and u.quickPin = :quickPin
			""")
	Optional<User> findByCompanyIdAndQuickPin(Long companyId, String quickPin);

	@Query("""
			select count(u)
			from User u
			where u.company.id = :companyId
			  and u.status = 'ACTIVO'
			""")
	long countActiveUsers(Long companyId);

	Optional<User> findById(Long id);

    List<User> findByCompanyIdOrderByFullNameAsc(Long companyId);

    boolean existsByCompanyIdAndCedulaIgnoreCase(Long companyId, String cedula);

    boolean existsByCompanyIdAndCedulaIgnoreCaseAndIdNot(Long companyId, String cedula, Long id);

    boolean existsByCompanyIdAndQuickPin(Long companyId, String quickPin);

    boolean existsByCompanyIdAndQuickPinAndIdNot(Long companyId, String quickPin, Long id);
}

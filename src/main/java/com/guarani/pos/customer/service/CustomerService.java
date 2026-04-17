package com.guarani.pos.customer.service;

import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.customer.dto.CustomerRequest;
import com.guarani.pos.customer.dto.CustomerResponse;
import com.guarani.pos.customer.model.Customer;
import com.guarani.pos.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;

    public CustomerService(CustomerRepository customerRepository,
                           CompanyRepository companyRepository) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll(Long companyId, String q) {
        List<Customer> items = (q == null || q.isBlank())
                ? customerRepository.findByCompanyIdOrderByNombreAsc(companyId)
                : customerRepository.search(companyId, q.trim());

        return items.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long companyId, Long id) {
        Customer customer = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse create(Long companyId, CustomerRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        validateDuplicates(companyId, request, null);

        Customer customer = new Customer();
        customer.setCompany(company);
        apply(customer, request);

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long companyId, Long id, CustomerRequest request) {
        Customer customer = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

        validateDuplicates(companyId, request, id);
        apply(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void changeStatus(Long companyId, Long id, boolean active) {
        Customer customer = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
        customer.setActivo(active);
        customerRepository.save(customer);
    }

    private void apply(Customer customer, CustomerRequest request) {
        customer.setNombre(request.nombre().trim());
        customer.setDocumento(trimToNull(request.documento()));
        customer.setDocumentType(request.documentType());
        customer.setRuc(trimToNull(request.ruc()));
        customer.setTelefono(trimToNull(request.telefono()));
        customer.setEmail(trimToNull(request.email()));
        customer.setDireccion(trimToNull(request.direccion()));
        customer.setGender(request.gender());
        customer.setSegment(request.segment());
        customer.setTaxProfile(request.taxProfile());
        customer.setObservacion(trimToNull(request.observacion()));
        customer.setActivo(request.activo());
    }

    private void validateDuplicates(Long companyId, CustomerRequest request, Long currentId) {
        String documento = trimToNull(request.documento());
        String ruc = trimToNull(request.ruc());

        if (documento != null) {
            boolean exists = currentId == null
                    ? customerRepository.existsByCompanyIdAndDocumentoIgnoreCase(companyId, documento)
                    : customerRepository.existsByCompanyIdAndDocumentoIgnoreCaseAndIdNot(companyId, documento, currentId);
            if (exists) {
                throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
            }
        }

        if (ruc != null) {
            boolean exists = currentId == null
                    ? customerRepository.existsByCompanyIdAndRucIgnoreCase(companyId, ruc)
                    : customerRepository.existsByCompanyIdAndRucIgnoreCaseAndIdNot(companyId, ruc, currentId);
            if (exists) {
                throw new IllegalArgumentException("Ya existe un cliente con ese RUC.");
            }
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getNombre(),
                c.getDocumento(),
                c.getDocumentType(),
                c.getRuc(),
                c.getTelefono(),
                c.getEmail(),
                c.getDireccion(),
                c.getGender(),
                c.getSegment(),
                c.getTaxProfile(),
                c.getObservacion(),
                c.isActivo()
        );
    }
}

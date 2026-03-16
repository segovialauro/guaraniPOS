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

        Customer customer = new Customer();
        customer.setCompany(company);
        apply(customer, request);

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long companyId, Long id, CustomerRequest request) {
        Customer customer = customerRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));

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
        customer.setDocumento(request.documento());
        customer.setRuc(request.ruc());
        customer.setTelefono(request.telefono());
        customer.setEmail(request.email());
        customer.setDireccion(request.direccion());
        customer.setObservacion(request.observacion());
        customer.setActivo(request.activo());
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getNombre(),
                c.getDocumento(),
                c.getRuc(),
                c.getTelefono(),
                c.getEmail(),
                c.getDireccion(),
                c.getObservacion(),
                c.isActivo()
        );
    }
}
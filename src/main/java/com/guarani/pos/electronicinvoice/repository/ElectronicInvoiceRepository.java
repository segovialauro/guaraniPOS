package com.guarani.pos.electronicinvoice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.electronicinvoice.model.ElectronicInvoice;

public interface ElectronicInvoiceRepository extends JpaRepository<ElectronicInvoice, Long> {

    Optional<ElectronicInvoice> findBySale_IdAndCompany_Id(Long saleId, Long companyId);

    List<ElectronicInvoice> findTop50ByCompany_IdOrderByCreatedAtDesc(Long companyId);
}

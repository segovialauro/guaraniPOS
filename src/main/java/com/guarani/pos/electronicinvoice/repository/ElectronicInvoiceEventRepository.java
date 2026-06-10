package com.guarani.pos.electronicinvoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.electronicinvoice.model.ElectronicInvoiceEvent;

public interface ElectronicInvoiceEventRepository extends JpaRepository<ElectronicInvoiceEvent, Long> {
}

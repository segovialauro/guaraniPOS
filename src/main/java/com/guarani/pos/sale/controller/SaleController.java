package com.guarani.pos.sale.controller;

import com.guarani.pos.sale.dto.SaleCreateRequest;
import com.guarani.pos.sale.dto.SaleResponse;
import com.guarani.pos.sale.service.SaleService;
import com.guarani.pos.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.guarani.pos.sale.dto.SaleTicketResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/ventas")
public class SaleController {

	private final SaleService saleService;

	public SaleController(SaleService saleService) {
		this.saleService = saleService;
	}

	@GetMapping
	public List<SaleResponse> findRecent() {
		return saleService.findRecent(SecurityUtils.getCurrentCompanyId());
	}

	@PostMapping
	public SaleResponse create(@Valid @RequestBody SaleCreateRequest request) {
		return saleService.create(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), request);
	}

	@GetMapping("/{id}/ticket")
	public SaleTicketResponse ticket(@PathVariable Long id) {
		return saleService.getTicket(SecurityUtils.getCurrentCompanyId(), SecurityUtils.getCurrentUserId(), id);
	}

	@GetMapping("/{id}/ticket/pdf")
	public ResponseEntity<byte[]> ticketPdf(@PathVariable Long id) {
		byte[] pdf = saleService.generateTicketPdf(SecurityUtils.getCurrentCompanyId(),
				SecurityUtils.getCurrentUserId(), id);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket-" + id + ".pdf")
				.contentType(MediaType.APPLICATION_PDF).body(pdf);
	}
}
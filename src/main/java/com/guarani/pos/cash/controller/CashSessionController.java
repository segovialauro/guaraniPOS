package com.guarani.pos.cash.controller;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.guarani.pos.cash.dto.CashCloseReportResponse;
import com.guarani.pos.cash.dto.CashCloseRequest;
import com.guarani.pos.cash.dto.CashMovementCancelRequest;
import com.guarani.pos.cash.dto.CashMovementRequest;
import com.guarani.pos.cash.dto.CashMovementResponse;
import com.guarani.pos.cash.dto.CashMovementUpdateRequest;
import com.guarani.pos.cash.dto.CashOpenRequest;
import com.guarani.pos.cash.dto.CashSessionResponse;
import com.guarani.pos.cash.service.CashSessionService;
import com.guarani.pos.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/caja")
public class CashSessionController {

    private final CashSessionService cashSessionService;

    public CashSessionController(CashSessionService cashSessionService) {
        this.cashSessionService = cashSessionService;
    }

    @GetMapping("/actual")
    public CashSessionResponse getCurrent() {
        return cashSessionService.getCurrent(SecurityUtils.getCurrentCompanyId());
    }

    @PostMapping("/abrir")
    public CashSessionResponse abrir(@Valid @RequestBody CashOpenRequest request) {
        return cashSessionService.open(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                request
        );
    }

    @PostMapping("/cerrar")
    public CashSessionResponse cerrar(@Valid @RequestBody CashCloseRequest request) {
        return cashSessionService.close(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                request
        );
    }
    
    @GetMapping("/historial")
    public List<CashSessionResponse> historial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status) {
        return cashSessionService.getHistory(SecurityUtils.getCurrentCompanyId(), from, to, status);
    }

    @GetMapping("/{id}/reporte")
    public CashCloseReportResponse reporte(@PathVariable Long id) {
        return cashSessionService.getCloseReport(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                id
        );
    }
    
    @GetMapping("/{id}/reporte/pdf")
    public ResponseEntity<byte[]> reportePdf(@PathVariable Long id) {
        byte[] pdf = cashSessionService.generateCloseReportPdf(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                id
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cierre-caja-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    
    @GetMapping("/movimientos/actual")
    public List<CashMovementResponse> movimientosActuales() {
        return cashSessionService.getCurrentMovements(SecurityUtils.getCurrentCompanyId());
    }
    
    @PostMapping("/movimientos")
    public CashMovementResponse registrarMovimiento(@Valid @RequestBody CashMovementRequest request) {
        return cashSessionService.registerMovement(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                request
        );
    }

    @PutMapping("/movimientos/{id}")
    public CashMovementResponse editarMovimiento(
            @PathVariable Long id,
            @Valid @RequestBody CashMovementUpdateRequest request
    ) {
        return cashSessionService.updateMovement(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                id,
                request
        );
    }

    @PostMapping("/movimientos/{id}/anular")
    public CashMovementResponse anularMovimiento(
            @PathVariable Long id,
            @Valid @RequestBody CashMovementCancelRequest request
    ) {
        return cashSessionService.cancelMovement(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                id,
                request
        );
    }


}

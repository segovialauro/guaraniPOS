package com.guarani.pos.pagare.controller;

import com.guarani.pos.pagare.dto.PagareCreateRequest;
import com.guarani.pos.pagare.dto.PagarePaymentRequest;
import com.guarani.pos.pagare.dto.PagareResponse;
import com.guarani.pos.pagare.service.PagareService;
import com.guarani.pos.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagares")
public class PagareController {

    private final PagareService pagareService;

    public PagareController(PagareService pagareService) {
        this.pagareService = pagareService;
    }

    @GetMapping
    public List<PagareResponse> findRecent() {
        return pagareService.findRecent(SecurityUtils.getCurrentCompanyId());
    }

    @GetMapping("/auditoria")
    public List<PagareResponse> findAuditHistory(@RequestParam(required = false) String from,
                                                 @RequestParam(required = false) String to,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String q) {
        return pagareService.findAuditHistory(
                SecurityUtils.getCurrentCompanyId(),
                from,
                to,
                status,
                q
        );
    }

    @PostMapping
    public PagareResponse create(@Valid @RequestBody PagareCreateRequest request) {
        return pagareService.create(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                request
        );
    }

    @PostMapping("/{id}/payments")
    public PagareResponse registerPayment(@PathVariable Long id, @Valid @RequestBody PagarePaymentRequest request) {
        return pagareService.registerPayment(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                id,
                request
        );
    }
}

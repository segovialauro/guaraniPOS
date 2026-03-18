package com.guarani.pos.billing.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guarani.pos.billing.dto.BillingConfigRequest;
import com.guarani.pos.billing.dto.BillingConfigResponse;
import com.guarani.pos.billing.model.BillingConfig;
import com.guarani.pos.billing.repository.BillingConfigRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.subscription.service.SubscriptionAccessService;

@Service
public class BillingConfigService {

    private final BillingConfigRepository billingConfigRepository;
    private final CompanyRepository companyRepository;
    private final SubscriptionAccessService subscriptionAccessService;

    public BillingConfigService(BillingConfigRepository billingConfigRepository,
                                CompanyRepository companyRepository,
                                SubscriptionAccessService subscriptionAccessService) {
        this.billingConfigRepository = billingConfigRepository;
        this.companyRepository = companyRepository;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Transactional(readOnly = true)
    public BillingConfigResponse getCurrent(Long companyId) {
        return billingConfigRepository.findFirstByCompany_IdOrderByIdDesc(companyId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public BillingConfigResponse save(Long companyId, BillingConfigRequest request) {
        String documentType = normalize(request.documentType());

        validatePlanAccess(companyId, documentType);

        BillingConfig config = billingConfigRepository.findFirstByCompany_IdOrderByIdDesc(companyId)
                .orElseGet(() -> {
                    Company company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));
                    BillingConfig created = new BillingConfig();
                    created.setCompany(company);
                    return created;
                });

        config.setDocumentType(documentType);
        config.setPrinterBrand(trimToNull(request.printerBrand()));
        config.setPrinterModel(trimToNull(request.printerModel()));
        config.setPrinterName(trimToNull(request.printerName()));
        config.setEstablishmentCode(trimToNull(request.establishmentCode()));
        config.setExpeditionPoint(trimToNull(request.expeditionPoint()));
        config.setInvoiceFooter(trimToNull(request.invoiceFooter()));
        config.setSifenEnvironment(trimToNull(request.sifenEnvironment()));
        config.setActive(request.active());

        return toResponse(billingConfigRepository.save(config));
    }

    private void validatePlanAccess(Long companyId, String documentType) {
        switch (documentType) {
            case "INTERNO" -> subscriptionAccessService.validateInternalTicketEnabled(companyId);
            case "FISCAL_PRINTER" -> subscriptionAccessService.validateFiscalPrinterEnabled(companyId);
            case "ELECTRONICO" -> subscriptionAccessService.validateElectronicInvoiceEnabled(companyId);
            default -> throw new IllegalArgumentException("Tipo de documento no soportado: " + documentType);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BillingConfigResponse toResponse(BillingConfig config) {
        return new BillingConfigResponse(
                config.getId(),
                config.getDocumentType(),
                config.getPrinterBrand(),
                config.getPrinterModel(),
                config.getPrinterName(),
                config.getEstablishmentCode(),
                config.getExpeditionPoint(),
                config.getInvoiceFooter(),
                config.getSifenEnvironment(),
                config.isActive()
        );
    }
}
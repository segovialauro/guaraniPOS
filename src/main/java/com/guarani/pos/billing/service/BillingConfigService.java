package com.guarani.pos.billing.service;

import java.time.LocalDate;
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
                .map(config -> toResponse(companyId, config))
                .orElseGet(() -> emptyResponse(companyId));
    }

    @Transactional
    public BillingConfigResponse save(Long companyId, BillingConfigRequest request) {
        String documentType = normalize(request.documentType());

        validatePlanAccess(companyId, documentType);
        validateFiscalRules(request);

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
        config.setCommercialName(trimToNull(request.commercialName()));
        config.setLegalName(trimToNull(request.legalName()));
        config.setRuc(trimToNull(request.ruc()));
        config.setPhone(trimToNull(request.phone()));
        config.setAddress(trimToNull(request.address()));
        config.setBranchName(trimToNull(request.branchName()));
        config.setTimbradoNumber(trimToNull(request.timbradoNumber()));
        config.setTimbradoValidity(trimToNull(request.timbradoValidity()));
        config.setInvoiceNumber(trimToNull(request.invoiceNumber()));
        config.setTaxpayerType(trimToNull(request.taxpayerType()));
        config.setTaxRegimeCode(trimToNull(request.taxRegimeCode()));
        config.setEconomicActivityCode(trimToNull(request.economicActivityCode()));
        config.setQrSecurityCodeId(trimToNull(request.qrSecurityCodeId()));
        config.setQrSecurityCode(trimToNull(request.qrSecurityCode()));
        config.setElectronicSeries(trimToNull(normalizeSeries(request.electronicSeries())));
        config.setLogoDataUrl(trimToNull(request.logoDataUrl()));
        config.setShowSeller(request.showSeller());
        config.setShowVatBreakdown(request.showVatBreakdown());
        config.setShowSetQr(request.showSetQr());
        config.setShowItemDiscount(request.showItemDiscount());
        config.setActive(request.active());

        return toResponse(companyId, billingConfigRepository.save(config));
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

    private String normalizeSeries(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private void validateFiscalRules(BillingConfigRequest request) {
        LocalDate validityDate = LocalDate.parse(request.timbradoValidity());
        if (validityDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La vigencia del timbrado ya esta vencida.");
        }

        if ("ELECTRONICO".equalsIgnoreCase(request.documentType())) {
            if (trimToNull(request.taxpayerType()) == null) {
                throw new IllegalArgumentException("Debe indicar el tipo de contribuyente para factura electronica.");
            }
            if (trimToNull(request.taxRegimeCode()) == null) {
                throw new IllegalArgumentException("Debe indicar el regimen tributario para factura electronica.");
            }
            if (trimToNull(request.economicActivityCode()) == null) {
                throw new IllegalArgumentException("Debe indicar el codigo de actividad economica para factura electronica.");
            }
            if (trimToNull(request.qrSecurityCodeId()) == null) {
                throw new IllegalArgumentException("Debe indicar el Id CSC para factura electronica.");
            }
            if (trimToNull(request.qrSecurityCode()) == null) {
                throw new IllegalArgumentException("Debe indicar el CSC para factura electronica.");
            }
        }
    }

    private BillingConfigResponse emptyResponse(Long companyId) {
        boolean electronicAllowed = isElectronicInvoiceAllowed(companyId);
        return new BillingConfigResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                electronicAllowed ? "NO_CONFIGURADO" : "PLAN_REQUERIDO",
                electronicAllowed
                        ? "Aun no configuraste la factura electronica. Puedes cargar los datos SIFEN y dejarla lista para cuando tengas la firma digital."
                        : "Tu plan actual no incluye factura electronica. Debes pasar a Premium para habilitar esta configuracion.",
                null,
                false,
                false,
                false,
                false,
                false
        );
    }

    private BillingConfigResponse toResponse(Long companyId, BillingConfig config) {
        String setupStatus = resolveElectronicSetupStatus(companyId, config);
        String setupMessage = resolveElectronicSetupMessage(setupStatus);
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
                config.getCommercialName(),
                config.getLegalName(),
                config.getRuc(),
                config.getPhone(),
                config.getAddress(),
                config.getBranchName(),
                config.getTimbradoNumber(),
                config.getTimbradoValidity(),
                config.getInvoiceNumber(),
                config.getTaxpayerType(),
                config.getTaxRegimeCode(),
                config.getEconomicActivityCode(),
                config.getQrSecurityCodeId(),
                config.getQrSecurityCode(),
                config.getElectronicSeries(),
                setupStatus,
                setupMessage,
                config.getLogoDataUrl(),
                config.isShowSeller(),
                config.isShowVatBreakdown(),
                config.isShowSetQr(),
                config.isShowItemDiscount(),
                config.isActive()
        );
    }

    private String resolveElectronicSetupStatus(Long companyId, BillingConfig config) {
        if (!isElectronicInvoiceAllowed(companyId)) {
            return "PLAN_REQUERIDO";
        }
        if (!"ELECTRONICO".equalsIgnoreCase(config.getDocumentType())) {
            return "NO_CONFIGURADO";
        }
        if (!hasElectronicCoreData(config)) {
            return "CONFIGURACION_INCOMPLETA";
        }
        return "PENDIENTE_FIRMA_DIGITAL";
    }

    private String resolveElectronicSetupMessage(String status) {
        return switch (status) {
            case "PLAN_REQUERIDO" ->
                    "Tu plan actual no incluye factura electronica. Debes pasar a Premium para habilitar esta configuracion.";
            case "NO_CONFIGURADO" ->
                    "La empresa aun no activo la modalidad electronica. Puedes cargar los datos preparatorios cuando quieras.";
            case "CONFIGURACION_INCOMPLETA" ->
                    "Faltan datos SIFEN obligatorios. Completa la configuracion fiscal antes de intentar emitir electronicamente.";
            case "PENDIENTE_FIRMA_DIGITAL" ->
                    "La base SIFEN ya esta cargada. El siguiente paso es obtener y vincular la firma digital para pasar a homologacion/emision.";
            default ->
                    "Estado de configuracion electronica no determinado.";
        };
    }

    private boolean hasElectronicCoreData(BillingConfig config) {
        return trimToNull(config.getCommercialName()) != null
                && trimToNull(config.getLegalName()) != null
                && trimToNull(config.getRuc()) != null
                && trimToNull(config.getAddress()) != null
                && trimToNull(config.getBranchName()) != null
                && trimToNull(config.getTimbradoNumber()) != null
                && trimToNull(config.getTimbradoValidity()) != null
                && trimToNull(config.getInvoiceNumber()) != null
                && trimToNull(config.getEstablishmentCode()) != null
                && trimToNull(config.getExpeditionPoint()) != null
                && trimToNull(config.getTaxpayerType()) != null
                && trimToNull(config.getTaxRegimeCode()) != null
                && trimToNull(config.getEconomicActivityCode()) != null
                && trimToNull(config.getQrSecurityCodeId()) != null
                && trimToNull(config.getQrSecurityCode()) != null;
    }

    private boolean isElectronicInvoiceAllowed(Long companyId) {
        try {
            subscriptionAccessService.validateElectronicInvoiceEnabled(companyId);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

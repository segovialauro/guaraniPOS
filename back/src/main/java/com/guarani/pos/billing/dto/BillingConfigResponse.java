package com.guarani.pos.billing.dto;

public record BillingConfigResponse(
        Long id,
        String documentType,
        String printerBrand,
        String printerModel,
        String printerName,
        String establishmentCode,
        String expeditionPoint,
        String invoiceFooter,
        String sifenEnvironment,
        boolean active
) {
}
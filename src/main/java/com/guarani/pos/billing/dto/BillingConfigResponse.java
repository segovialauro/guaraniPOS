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
        String commercialName,
        String legalName,
        String ruc,
        String phone,
        String address,
        String branchName,
        String timbradoNumber,
        String timbradoValidity,
        String invoiceNumber,
        String logoDataUrl,
        boolean showSeller,
        boolean showVatBreakdown,
        boolean showSetQr,
        boolean showItemDiscount,
        boolean active
) {
}

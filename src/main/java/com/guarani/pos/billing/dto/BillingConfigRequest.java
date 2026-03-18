package com.guarani.pos.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BillingConfigRequest(
        @NotBlank
        @Size(max = 30)
        String documentType,

        @Size(max = 100)
        String printerBrand,

        @Size(max = 100)
        String printerModel,

        @Size(max = 150)
        String printerName,

        @Size(max = 20)
        String establishmentCode,

        @Size(max = 20)
        String expeditionPoint,

        @Size(max = 500)
        String invoiceFooter,

        @Size(max = 20)
        String sifenEnvironment,

        @Size(max = 150)
        String commercialName,

        @Size(max = 150)
        String legalName,

        @Size(max = 30)
        String ruc,

        @Size(max = 50)
        String phone,

        @Size(max = 250)
        String address,

        @Size(max = 100)
        String branchName,

        @Size(max = 50)
        String timbradoNumber,

        @Size(max = 50)
        String timbradoValidity,

        @Size(max = 50)
        String invoiceNumber,

        String logoDataUrl,

        boolean showSeller,

        boolean showVatBreakdown,

        boolean showSetQr,

        boolean showItemDiscount,

        boolean active
) {
}

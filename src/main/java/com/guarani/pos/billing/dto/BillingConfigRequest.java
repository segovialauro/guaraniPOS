package com.guarani.pos.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^\\d{3}$", message = "El establecimiento debe tener 3 digitos.")
        String establishmentCode,

        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^\\d{3}$", message = "El punto de expedicion debe tener 3 digitos.")
        String expeditionPoint,

        @Size(max = 500)
        String invoiceFooter,

        @Size(max = 20)
        String sifenEnvironment,

        @NotBlank
        @Size(max = 150)
        String commercialName,

        @NotBlank
        @Size(max = 150)
        String legalName,

        @NotBlank
        @Size(max = 30)
        String ruc,

        @Size(max = 50)
        String phone,

        @NotBlank
        @Size(max = 250)
        String address,

        @NotBlank
        @Size(max = 100)
        String branchName,

        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^\\d{7,}$", message = "El timbrado debe contener solo digitos.")
        String timbradoNumber,

        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La vigencia debe tener formato YYYY-MM-DD.")
        String timbradoValidity,

        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = "^\\d{3}-\\d{3}-\\d{7}$",
                message = "El proximo numero debe tener formato 001-001-0000001."
        )
        String invoiceNumber,

        String logoDataUrl,

        boolean showSeller,

        boolean showVatBreakdown,

        boolean showSetQr,

        boolean showItemDiscount,

        boolean active
) {
}

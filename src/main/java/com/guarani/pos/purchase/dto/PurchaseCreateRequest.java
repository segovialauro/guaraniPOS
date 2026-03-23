package com.guarani.pos.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseCreateRequest(
        @NotNull
        Long supplierId,

        @NotBlank @Size(max = 20)
        String purchaseType,

        @NotBlank @Size(max = 50)
        String invoiceNumber,

        @NotNull
        LocalDate purchaseDate,

        LocalDate dueDate,

        @NotBlank @Size(max = 20)
        String paymentCondition,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal initialPayment,

        @Size(max = 30)
        String initialPaymentMethod,

        @Size(max = 500)
        String observation,

        @Valid
        List<PurchaseItemRequest> items
) {
}

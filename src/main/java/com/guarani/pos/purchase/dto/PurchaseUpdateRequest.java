package com.guarani.pos.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PurchaseUpdateRequest(
        @NotNull
        Long supplierId,

        @NotBlank
        @Size(max = 50)
        String invoiceNumber,

        @NotNull
        LocalDate purchaseDate,

        LocalDate dueDate,

        @NotBlank
        @Size(max = 20)
        String paymentCondition,

        @Size(max = 500)
        String observation,

        @Valid
        List<PurchaseItemRequest> items
) {
}

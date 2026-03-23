package com.guarani.pos.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PurchaseReceiveRequest(
        @Size(max = 500)
        String observation,

        @Valid
        List<PurchaseReceiveItemRequest> items
) {
}

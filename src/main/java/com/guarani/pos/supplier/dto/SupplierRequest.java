package com.guarani.pos.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank @Size(max = 150)
        String name,

        @Size(max = 30)
        String ruc,

        @Size(max = 30)
        String phone,

        @Size(max = 150)
        String email,

        @Size(max = 250)
        String address,

        @Size(max = 500)
        String observation,

        boolean active
) {
}

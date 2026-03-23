package com.guarani.pos.supplier.dto;

import java.math.BigDecimal;

public record SupplierResponse(
        Long id,
        String name,
        String ruc,
        String phone,
        String email,
        String address,
        String observation,
        boolean active,
        BigDecimal payableBalance
) {
}

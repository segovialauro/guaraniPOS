package com.guarani.pos.budget.dto;

import java.math.BigDecimal;

public record BudgetDetailResponse(
        Long productId,
        String productCode,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}

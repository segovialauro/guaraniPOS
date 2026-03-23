package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SalesOperationalCashierSummaryResponse(
        Long userId,
        String cashierName,
        long confirmedCount,
        BigDecimal confirmedTotal,
        long canceledCount,
        BigDecimal canceledTotal,
        long partialReturnCount,
        BigDecimal partialReturnTotal,
        long totalReturnCount,
        BigDecimal totalReturnTotal
) {
}

package com.guarani.pos.sale.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesOperationalSummaryResponse(
        String date,
        long confirmedCount,
        BigDecimal confirmedTotal,
        long canceledCount,
        BigDecimal canceledTotal,
        long partialReturnCount,
        BigDecimal partialReturnTotal,
        long totalReturnCount,
        BigDecimal totalReturnTotal,
        List<SalesOperationalCashierSummaryResponse> cashiers
) {
}

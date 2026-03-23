package com.guarani.pos.purchase.dto;

import java.math.BigDecimal;

public record PurchaseSummaryResponse(
        BigDecimal monthlyTotal,
        BigDecimal pendingTotal,
        BigDecimal payableTotal,
        long pendingCount
) {
}

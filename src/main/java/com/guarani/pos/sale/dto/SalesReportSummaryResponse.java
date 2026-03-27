package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SalesReportSummaryResponse(
        String fromDate,
        String toDate,
        long salesCount,
        BigDecimal grossTotal,
        BigDecimal discountTotal,
        BigDecimal returnTotal,
        BigDecimal netTotal,
        BigDecimal averageTicket
) {
}

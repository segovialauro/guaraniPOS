package com.guarani.pos.sale.dto;

import java.util.List;

public record SalesReportResponse(
        SalesReportSummaryResponse summary,
        List<SalesByDayReportResponse> byDay,
        List<SalesByCashierReportResponse> byCashier,
        List<TopProductReportResponse> topProducts
) {
}

package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SalesByDayReportResponse(
        String date,
        long salesCount,
        BigDecimal grossTotal,
        BigDecimal discountTotal,
        BigDecimal returnTotal,
        BigDecimal netTotal,
        BigDecimal averageTicket
) {
}

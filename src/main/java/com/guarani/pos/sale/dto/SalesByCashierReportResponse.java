package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SalesByCashierReportResponse(
        Long cashierId,
        String cashierName,
        long salesCount,
        BigDecimal grossTotal,
        BigDecimal discountTotal,
        BigDecimal returnTotal,
        BigDecimal netTotal,
        BigDecimal averageTicket
) {
}

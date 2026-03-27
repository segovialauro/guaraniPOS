package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record TopProductReportResponse(
        Long productId,
        String productCode,
        String productName,
        BigDecimal quantity,
        BigDecimal netTotal
) {
}

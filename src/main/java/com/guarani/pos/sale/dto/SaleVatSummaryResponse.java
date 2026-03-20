package com.guarani.pos.sale.dto;

import java.math.BigDecimal;

public record SaleVatSummaryResponse(
        BigDecimal taxableVat10,
        BigDecimal taxableVat5,
        BigDecimal exemptTotal,
        BigDecimal vat10,
        BigDecimal vat5,
        BigDecimal totalVat
) {
}

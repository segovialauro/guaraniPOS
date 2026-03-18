package com.guarani.pos.subscription.dto;

import java.math.BigDecimal;

public record PublicPlanResponse(
        String code,
        String name,
        String description,
        BigDecimal priceMonthly,
        Integer maxOpenCashSessions,
        boolean allowInternalTicket,
        boolean allowFiscalPrinter,
        boolean allowElectronicInvoice,
        boolean allowBancardQr
) {
}
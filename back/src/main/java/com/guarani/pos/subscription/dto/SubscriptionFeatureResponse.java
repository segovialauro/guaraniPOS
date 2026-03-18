package com.guarani.pos.subscription.dto;

public record SubscriptionFeatureResponse(
        String planCode,
        String planName,
        Integer maxOpenCashSessions,
        Integer maxUsers,
        Integer maxBranches,
        boolean allowInternalTicket,
        boolean allowFiscalPrinter,
        boolean allowElectronicInvoice,
        boolean allowBancardQr
) {
}
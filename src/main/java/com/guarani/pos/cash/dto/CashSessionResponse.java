package com.guarani.pos.cash.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashSessionResponse(
        Long id,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        BigDecimal openingAmount,
        BigDecimal cashSystem,
        BigDecimal transferSystem,
        BigDecimal debitCardSystem,
        BigDecimal creditCardSystem,
        BigDecimal qrSystem,
        BigDecimal totalSystem,
        BigDecimal manualIncomeTotal,
        BigDecimal manualWithdrawalTotal,
        BigDecimal expectedCash,
        BigDecimal cashCounted,
        BigDecimal difference,
        String status,
        String observation
) {
}

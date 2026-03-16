package com.guarani.pos.cash.dto;

import com.guarani.pos.cash.model.CashMovementStatus;
import com.guarani.pos.cash.model.CashMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashMovementResponse(
        Long id,
        Long cashSessionId,
        LocalDateTime createdAt,
        CashMovementType type,
        BigDecimal amount,
        String description,
        String username,
        CashMovementStatus status,
        LocalDateTime updatedAt,
        String updatedByUsername,
        LocalDateTime canceledAt,
        String canceledByUsername,
        String cancellationReason
) {
}

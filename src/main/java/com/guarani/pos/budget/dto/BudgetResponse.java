package com.guarani.pos.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BudgetResponse(
        Long id,
        String budgetNumber,
        LocalDateTime date,
        LocalDate validUntil,
        Long customerId,
        String customerName,
        String status,
        String observation,
        Long saleId,
        BigDecimal subtotalBeforeDiscounts,
        BigDecimal discountTotal,
        BigDecimal globalDiscountAmount,
        BigDecimal total,
        List<BudgetDetailResponse> items
) {
}

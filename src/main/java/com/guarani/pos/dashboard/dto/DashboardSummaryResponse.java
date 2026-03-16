package com.guarani.pos.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        String companyName,
        long todaySalesCount,
        BigDecimal todaySalesAmount,
        long monthSalesCount,
        BigDecimal monthSalesAmount,
        long lowStockCount,
        long pendingBudgetsCount,
        long overduePromissoryNotesCount,
        long activeUsersCount
) {}

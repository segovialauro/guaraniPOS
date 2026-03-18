package com.guarani.pos.dashboard.service;

import com.guarani.pos.auth.repository.UserRepository;
import com.guarani.pos.budget.repository.BudgetRepository;
import com.guarani.pos.company.model.Company;
import com.guarani.pos.company.repository.CompanyRepository;
import com.guarani.pos.dashboard.dto.DashboardSummaryResponse;
import com.guarani.pos.pagare.repository.PagareRepository;
import com.guarani.pos.product.repository.StockRepository;
import com.guarani.pos.sale.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final SaleRepository saleRepository;
    private final StockRepository stockRepository;
    private final BudgetRepository budgetRepository;
    private final PagareRepository pagareRepository;
    private final UserRepository userRepository;

    public DashboardService(CompanyRepository companyRepository,
                            SaleRepository saleRepository,
                            StockRepository stockRepository,
                            BudgetRepository budgetRepository,
                            PagareRepository pagareRepository,
                            UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.saleRepository = saleRepository;
        this.stockRepository = stockRepository;
        this.budgetRepository = budgetRepository;
        this.pagareRepository = pagareRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        LocalDate today = LocalDate.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();

        long todaySalesCount = saleRepository.countByCompanyAndPeriod(companyId, todayStart, tomorrowStart);
        BigDecimal todaySalesAmount = saleRepository.sumByCompanyAndPeriod(companyId, todayStart, tomorrowStart);

        long monthSalesCount = saleRepository.countByCompanyAndPeriod(companyId, monthStart, tomorrowStart);
        BigDecimal monthSalesAmount = saleRepository.sumByCompanyAndPeriod(companyId, monthStart, tomorrowStart);

        long lowStockCount = stockRepository.countLowStock(companyId);
        long pendingBudgetsCount = budgetRepository.countPending(companyId);
        long overduePromissoryNotesCount = pagareRepository.countOverdue(companyId, today);//countOverdue no existe en pagarerepository
        long activeUsersCount = userRepository.countActiveUsers(companyId);

        return new DashboardSummaryResponse(
                company.getName(),
                todaySalesCount,
                todaySalesAmount == null ? BigDecimal.ZERO : todaySalesAmount,
                monthSalesCount,
                monthSalesAmount == null ? BigDecimal.ZERO : monthSalesAmount,
                lowStockCount,
                pendingBudgetsCount,
                overduePromissoryNotesCount,
                activeUsersCount
        );
    }
}
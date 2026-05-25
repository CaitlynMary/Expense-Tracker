package com.familyexpensetracker.module.report.service;

import com.familyexpensetracker.module.report.dto.BudgetUsageResponse;
import com.familyexpensetracker.module.report.dto.CategoryReportResponse;
import com.familyexpensetracker.module.report.dto.DashboardSummaryResponse;
import com.familyexpensetracker.module.report.dto.MonthlyReportResponse;
import com.familyexpensetracker.module.report.dto.SavingsReportResponse;
import com.familyexpensetracker.module.report.dto.MonthlyTrendResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    MonthlyReportResponse getMonthlyReport(LocalDate startDate, LocalDate endDate);

    List<CategoryReportResponse> getCategoryWiseReport(LocalDate startDate, LocalDate endDate);

    List<BudgetUsageResponse> getBudgetUsageReport(LocalDate startDate, LocalDate endDate);

    SavingsReportResponse getSavingsReport();

    DashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate);

    List<MonthlyTrendResponse> getMonthlyTrend(LocalDate endDate);
}

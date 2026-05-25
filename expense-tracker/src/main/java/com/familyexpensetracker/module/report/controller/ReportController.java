package com.familyexpensetracker.module.report.controller;

import com.familyexpensetracker.module.report.dto.MonthlyReportResponse;
import com.familyexpensetracker.module.report.dto.CategoryReportResponse;
import com.familyexpensetracker.module.report.dto.BudgetUsageResponse;
import com.familyexpensetracker.module.report.dto.SavingsReportResponse;
import com.familyexpensetracker.module.report.dto.DashboardSummaryResponse;
import com.familyexpensetracker.module.report.dto.MonthlyTrendResponse;
import com.familyexpensetracker.module.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller exposing reporting endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        MonthlyReportResponse response = reportService.getMonthlyReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category-wise")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<CategoryReportResponse>> getCategoryWiseReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryReportResponse> response = reportService.getCategoryWiseReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/budget-usage")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<BudgetUsageResponse>> getBudgetUsageReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BudgetUsageResponse> response = reportService.getBudgetUsageReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/savings")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<SavingsReportResponse> getSavingsReport() {
        SavingsReportResponse response = reportService.getSavingsReport();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DashboardSummaryResponse response = reportService.getDashboardSummary(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MonthlyTrendResponse> response = reportService.getMonthlyTrend(endDate);
        return ResponseEntity.ok(response);
    }
}

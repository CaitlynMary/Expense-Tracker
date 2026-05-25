package com.familyexpensetracker.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for monthly expense summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {
    private int year;
    private int month;
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private List<CategoryReportResponse> categoryBreakdown;
}

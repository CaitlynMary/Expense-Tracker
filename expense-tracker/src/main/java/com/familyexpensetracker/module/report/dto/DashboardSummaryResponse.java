package com.familyexpensetracker.module.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.familyexpensetracker.module.bill.dto.BillResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal totalExpense;
    private BigDecimal spentThisMonth;
    private BigDecimal totalIncome;
    private Double savingsProgress;
    private Integer upcomingBillsCount;
    private Integer pendingBillsCount;
    private Integer overdueBillsCount;
    private BigDecimal remainingBudget;
    private BigDecimal monthlyBudget;
    private BigDecimal totalBudget;
    private BigDecimal pendingBillsAmount;
    private BigDecimal savingsSavedAmount;
    private BigDecimal savingsTargetAmount;
    private String topSpendingCategory;
    private BigDecimal topSpendingAmount;
    private String highestExpenseTitle;
    private BigDecimal highestExpenseAmount;
    private String billDueSoonName;
    private LocalDate billDueSoonDate;
    
    // New fields for the redesigned Reports page
    private BigDecimal savingsAdded;
    private Integer billsPaidCount;
    private BigDecimal billsPaidAmount;
    private List<BillResponse> pendingBills;
}

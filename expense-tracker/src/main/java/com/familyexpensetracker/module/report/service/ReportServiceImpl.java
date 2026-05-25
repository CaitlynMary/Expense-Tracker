package com.familyexpensetracker.module.report.service;

import com.familyexpensetracker.common.mapper.BillMapper;
import com.familyexpensetracker.module.bill.entity.Bill;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import com.familyexpensetracker.module.bill.repository.BillRepository;
import com.familyexpensetracker.module.budget.entity.Budget;
import com.familyexpensetracker.module.budget.entity.CategoryBudget;
import com.familyexpensetracker.module.budget.repository.BudgetRepository;
import com.familyexpensetracker.module.expense.entity.Expense;
import com.familyexpensetracker.module.expense.repository.ExpenseRepository;
import com.familyexpensetracker.module.report.dto.*;
import com.familyexpensetracker.module.savings.entity.SavingsGoal;
import com.familyexpensetracker.module.savings.entity.SavingsTransaction;
import com.familyexpensetracker.module.savings.repository.SavingsGoalRepository;
import com.familyexpensetracker.module.savings.repository.SavingsTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final BillRepository billRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final BillMapper billMapper;

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    @Transactional
    public void initSavingsTransactions() {
        if (savingsTransactionRepository.count() == 0) {
            log.info("Initializing savings transactions for existing savings goals");
            List<SavingsGoal> goals = savingsGoalRepository.findAll();
            for (SavingsGoal goal : goals) {
                if (goal.getSavedAmount() != null && goal.getSavedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    LocalDate txDate = goal.getCreatedAt() != null ? goal.getCreatedAt().toLocalDate() : LocalDate.now();
                    savingsTransactionRepository.save(SavingsTransaction.builder()
                            .savingsGoalId(goal.getId())
                            .amount(goal.getSavedAmount())
                            .transactionDate(txDate)
                            .build());
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(LocalDate startDate, LocalDate endDate) {
        DateRange range = resolveRange(startDate, endDate);
        YearMonth current = YearMonth.from(range.start());

        log.info("Generating monthly report");

        List<Expense> monthExpenses =
                expenseRepository
                        .findByExpenseDateBetween(range.start(), range.end(), Pageable.unpaged())
                        .getContent();

        BigDecimal totalExpense = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = BigDecimal.ZERO;

        List<CategoryReportResponse> categoryBreakdown =
                monthExpenses.stream()
                        .collect(Collectors.groupingBy(
                                Expense::getCategory,
                                Collectors.mapping(
                                        Expense::getAmount,
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                                )
                        ))
                        .entrySet()
                        .stream()
                        .map(entry -> CategoryReportResponse.builder()
                                .category(entry.getKey())
                                .amount(entry.getValue())
                                .build())
                        .toList();

        return MonthlyReportResponse.builder()
                .year(current.getYear())
                .month(current.getMonthValue())
                .totalExpense(totalExpense)
                .totalIncome(totalIncome)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryReportResponse> getCategoryWiseReport(LocalDate startDate, LocalDate endDate) {
        DateRange range = resolveRange(startDate, endDate);

        List<Expense> expenses = expenseRepository
                .findByExpenseDateBetween(range.start(), range.end(), Pageable.unpaged())
                .getContent();

        Map<String, BigDecimal> groupedExpenses =
                expenses.stream()
                        .collect(Collectors.groupingBy(
                                Expense::getCategory,
                                Collectors.mapping(
                                        Expense::getAmount,
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                                )
                        ));

        return groupedExpenses.entrySet()
                .stream()
                .map(entry -> CategoryReportResponse.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetUsageResponse> getBudgetUsageReport(LocalDate startDate, LocalDate endDate) {
        DateRange range = resolveRange(startDate, endDate);

        List<Expense> monthExpenses =
                expenseRepository
                        .findByExpenseDateBetween(range.start(), range.end(), Pageable.unpaged())
                        .getContent();

        List<Budget> budgets = budgetsInRange(range);

        if (budgets.isEmpty()) {
            return Collections.emptyList();
        }

        return budgets.stream()
                .flatMap(budget -> buildBudgetUsageRows(budget, monthExpenses).stream())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsReportResponse getSavingsReport() {

        List<SavingsGoal> goals =
                savingsGoalRepository.findAll();

        BigDecimal totalGoalAmount =
                goals.stream()
                        .map(SavingsGoal::getTargetAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSavedAmount =
                goals.stream()
                        .map(SavingsGoal::getSavedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        double progressPercentage =
                totalGoalAmount.compareTo(BigDecimal.ZERO) == 0
                        ? 0.0
                        : totalSavedAmount
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                totalGoalAmount,
                                2,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue();

        return SavingsReportResponse.builder()
                .totalGoalAmount(totalGoalAmount)
                .totalSavedAmount(totalSavedAmount)
                .progressPercentage(progressPercentage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate) {
        DateRange range = resolveRange(startDate, endDate);

        List<Expense> monthExpenses =
                expenseRepository
                        .findByExpenseDateBetween(range.start(), range.end(), Pageable.unpaged())
                        .getContent();

        BigDecimal totalExpense =
                monthExpenses.stream()
                        .map(Expense::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Bill> pendingBills =
                billRepository.findAll()
                        .stream()
                        .filter(bill -> bill.getStatus() == BillStatus.PENDING || bill.getStatus() == BillStatus.OVERDUE)
                        .filter(bill -> !bill.getDueDate().isBefore(range.start()) && !bill.getDueDate().isAfter(range.end()))
                        .sorted(Comparator.comparing(Bill::getDueDate))
                        .toList();

        BigDecimal pendingBillsAmount = pendingBills.stream()
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueBillsCount =
                billRepository.findAll()
                        .stream()
                        .filter(bill -> bill.getStatus() == BillStatus.PENDING || bill.getStatus() == BillStatus.OVERDUE)
                        .filter(bill -> bill.getDueDate().isBefore(LocalDate.now()))
                        .count();

        SavingsReportResponse savings =
                getSavingsReport();

        BigDecimal totalBudget =
                budgetsInRange(range)
                        .stream()
                        .map(Budget::getTotalLimit)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBudget = totalBudget.subtract(totalExpense);

        Map<String, BigDecimal> groupedExpenses =
                monthExpenses.stream()
                        .collect(Collectors.groupingBy(
                                expense -> expense.getCategory() == null || expense.getCategory().isBlank()
                                        ? "Uncategorized"
                                        : expense.getCategory(),
                                Collectors.mapping(
                                        Expense::getAmount,
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                                )
                        ));

        Map.Entry<String, BigDecimal> topSpendingCategory = groupedExpenses.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        Expense highestExpense = monthExpenses.stream()
                .max(Comparator.comparing(Expense::getAmount))
                .orElse(null);

        Bill billDueSoon = pendingBills.stream()
                .findFirst()
                .orElse(null);

        // New Redesigned report metrics calculated dynamically based on selected range
        BigDecimal savingsAdded = savingsTransactionRepository.findByTransactionDateBetween(range.start(), range.end())
                .stream()
                .map(SavingsTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Bill> paidBills = billRepository.findAll()
                .stream()
                .filter(bill -> bill.getStatus() == BillStatus.PAID)
                .filter(bill -> bill.getPaidDate() != null && !bill.getPaidDate().isBefore(range.start()) && !bill.getPaidDate().isAfter(range.end()))
                .toList();

        int billsPaidCount = paidBills.size();
        BigDecimal billsPaidAmount = paidBills.stream()
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<com.familyexpensetracker.module.bill.dto.BillResponse> pendingBillsList = pendingBills.stream()
                .map(billMapper::toResponse)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalExpense(totalExpense)
                .spentThisMonth(totalExpense)
                .totalIncome(BigDecimal.ZERO)
                .savingsProgress(savings.getProgressPercentage())
                .upcomingBillsCount(pendingBills.size())
                .pendingBillsCount(pendingBillsList.size())
                .overdueBillsCount(Math.toIntExact(overdueBillsCount))
                .remainingBudget(remainingBudget)
                .monthlyBudget(totalBudget)
                .totalBudget(totalBudget)
                .pendingBillsAmount(pendingBillsAmount)
                .savingsSavedAmount(savings.getTotalSavedAmount())
                .savingsTargetAmount(savings.getTotalGoalAmount())
                .topSpendingCategory(topSpendingCategory != null ? topSpendingCategory.getKey() : null)
                .topSpendingAmount(topSpendingCategory != null ? topSpendingCategory.getValue() : BigDecimal.ZERO)
                .highestExpenseTitle(highestExpense != null ? highestExpense.getTitle() : null)
                .highestExpenseAmount(highestExpense != null ? highestExpense.getAmount() : BigDecimal.ZERO)
                .billDueSoonName(billDueSoon != null ? billDueSoon.getName() : null)
                .billDueSoonDate(billDueSoon != null ? billDueSoon.getDueDate() : null)
                .savingsAdded(savingsAdded)
                .billsPaidCount(billsPaidCount)
                .billsPaidAmount(billsPaidAmount)
                .pendingBills(pendingBillsList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyTrend(LocalDate endDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        List<MonthlyTrendResponse> trend = new ArrayList<>();

        // Get the last 6 months ending in the reference month
        for (int i = 5; i >= 0; i--) {
            LocalDate targetDate = end.minusMonths(i);
            YearMonth ym = YearMonth.from(targetDate);
            LocalDate startOfMonth = ym.atDay(1);
            LocalDate endOfMonth = ym.atEndOfMonth();

            BigDecimal monthlyExpense = expenseRepository
                    .findByExpenseDateBetween(startOfMonth, endOfMonth, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String monthName = ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH);

            trend.add(MonthlyTrendResponse.builder()
                    .monthName(monthName)
                    .month(ym.getMonthValue())
                    .year(ym.getYear())
                    .totalExpense(monthlyExpense)
                    .build());
        }

        return trend;
    }

    private List<BudgetUsageResponse> buildBudgetUsageRows(Budget budget, List<Expense> monthExpenses) {
        // Collect all categories that have either a defined limit OR have expenses in this month
        Map<String, BigDecimal> categoryLimits = new java.util.HashMap<>();
        if (budget.getCategoryBudgets() != null) {
            for (CategoryBudget cb : budget.getCategoryBudgets()) {
                categoryLimits.put(cb.getCategory().toLowerCase(), cb.getLimitAmount());
            }
        }

        java.util.Set<String> allCategories = new java.util.HashSet<>();
        allCategories.addAll(categoryLimits.keySet());
        for (Expense expense : monthExpenses) {
            if (expense.getCategory() != null) {
                allCategories.add(expense.getCategory().toLowerCase());
            }
        }

        // Return a row for each category, mapping spent amount against limit (defaulting to 0)
        return allCategories.stream()
                .map(category -> {
                    BigDecimal spent = monthExpenses.stream()
                            .filter(expense -> category.equalsIgnoreCase(expense.getCategory()))
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal limit = categoryLimits.getOrDefault(category, BigDecimal.ZERO);
                    
                    // Capitalize first letter of category name for better display
                    String displayName = capitalize(category);

                    return toBudgetUsageResponse(
                            budget.getId(),
                            displayName,
                            limit,
                            spent
                    );
                })
                .toList();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private BudgetUsageResponse toBudgetUsageResponse(Long budgetId, String category, BigDecimal allocatedAmount, BigDecimal spentAmount) {
        BigDecimal remainingAmount = allocatedAmount.subtract(spentAmount);
        double usagePercentage = allocatedAmount.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : spentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(allocatedAmount, 2, RoundingMode.HALF_UP)
                .doubleValue();

        return BudgetUsageResponse.builder()
                .budgetId(budgetId)
                .category(category)
                .allocatedAmount(allocatedAmount)
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .usagePercentage(usagePercentage)
                .build();
    }

    private DateRange resolveRange(LocalDate startDate, LocalDate endDate) {
        YearMonth current = YearMonth.now();
        LocalDate defaultStart = current.atDay(1);
        LocalDate defaultEnd = current.atEndOfMonth();
        LocalDate start = startDate != null ? startDate : defaultStart;
        LocalDate end = endDate != null ? endDate : defaultEnd;

        if (end.isBefore(start)) {
            return new DateRange(end, start);
        }

        return new DateRange(start, end);
    }

    private List<Budget> budgetsInRange(DateRange range) {
        List<YearMonth> months = monthsInRange(range);
        return budgetRepository.findAll()
                .stream()
                .filter(budget -> months.contains(YearMonth.of(budget.getYear(), budget.getMonth())))
                .toList();
    }

    private List<YearMonth> monthsInRange(DateRange range) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth cursor = YearMonth.from(range.start());
        YearMonth endMonth = YearMonth.from(range.end());

        while (!cursor.isAfter(endMonth)) {
            months.add(cursor);
            cursor = cursor.plusMonths(1);
        }

        return months;
    }

    private record DateRange(LocalDate start, LocalDate end) {
    }
}

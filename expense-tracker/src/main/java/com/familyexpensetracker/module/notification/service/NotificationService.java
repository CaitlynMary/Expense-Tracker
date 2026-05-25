package com.familyexpensetracker.module.notification.service;

import com.familyexpensetracker.module.bill.entity.Bill;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import com.familyexpensetracker.module.bill.repository.BillRepository;
import com.familyexpensetracker.module.budget.entity.Budget;
import com.familyexpensetracker.module.budget.repository.BudgetRepository;
import com.familyexpensetracker.module.expense.entity.Expense;
import com.familyexpensetracker.module.expense.repository.ExpenseRepository;
import com.familyexpensetracker.module.notification.dto.NotificationDto;
import com.familyexpensetracker.module.notification.dto.NotificationType;
import com.familyexpensetracker.module.savings.entity.SavingsGoal;
import com.familyexpensetracker.module.savings.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final BillRepository billRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public List<NotificationDto> getNotifications() {
        List<NotificationDto> notifications = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Bill Notifications
        List<Bill> upcomingBills = billRepository.findByStatusNotAndDueDateGreaterThanEqualOrderByDueDateAsc(BillStatus.PAID, today);
        for (Bill bill : upcomingBills) {
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, bill.getDueDate());
            if (daysUntilDue <= 3) {
                String message = daysUntilDue == 0 ? "due today" : (daysUntilDue == 1 ? "due tomorrow" : "due in " + daysUntilDue + " days");
                notifications.add(NotificationDto.builder()
                        .id("bill_upcoming_" + bill.getId())
                        .title("Upcoming Bill: " + bill.getName())
                        .message(bill.getName() + " is " + message + " (₹" + bill.getAmount() + ")")
                        .type(NotificationType.WARNING)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        List<Bill> overdueBills = billRepository.findByStatusNotAndDueDateLessThanOrderByDueDateAsc(BillStatus.PAID, today);
        for (Bill bill : overdueBills) {
            notifications.add(NotificationDto.builder()
                    .id("bill_overdue_" + bill.getId())
                    .title("Overdue Bill: " + bill.getName())
                    .message(bill.getName() + " was due on " + bill.getDueDate())
                    .type(NotificationType.ALERT)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // 2. Savings Notifications
        List<SavingsGoal> goals = savingsGoalRepository.findAll();
        for (SavingsGoal goal : goals) {
            if (goal.getSavedAmount() != null && goal.getTargetAmount() != null &&
                    goal.getSavedAmount().compareTo(goal.getTargetAmount()) < 0) {
                
                // Add a simple reminder if target date is approaching (within 30 days) and progress is low
                if (goal.getTargetDate() != null) {
                    long daysToTarget = java.time.temporal.ChronoUnit.DAYS.between(today, goal.getTargetDate());
                    if (daysToTarget >= 0 && daysToTarget <= 30) {
                        BigDecimal progress = goal.getSavedAmount().divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                        if (progress.compareTo(new BigDecimal("80")) < 0) {
                             notifications.add(NotificationDto.builder()
                                .id("savings_reminder_" + goal.getId())
                                .title("Savings Goal: " + goal.getName())
                                .message("Target date is near. Add money to reach your goal!")
                                .type(NotificationType.INFO)
                                .createdAt(LocalDateTime.now())
                                .build());
                        }
                    }
                }
            }
        }

        // 3. Budget Notifications
        YearMonth currentMonth = YearMonth.now();
        Optional<Budget> optionalBudget = budgetRepository.findByMonthAndYear(currentMonth.getMonthValue(), currentMonth.getYear());
        if (optionalBudget.isPresent()) {
            Budget budget = optionalBudget.get();
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();
            
            // Calculate total expenses for current month
            List<Expense> monthlyExpenses = expenseRepository.findByExpenseDateBetween(startOfMonth, endOfMonth, Pageable.unpaged()).getContent();
            BigDecimal totalSpent = monthlyExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (budget.getTotalLimit() != null && budget.getTotalLimit().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal usagePercentage = totalSpent.divide(budget.getTotalLimit(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                
                if (usagePercentage.compareTo(new BigDecimal("100")) >= 0) {
                     notifications.add(NotificationDto.builder()
                            .id("budget_exceeded_" + budget.getId())
                            .title("Budget Exceeded")
                            .message("You have exceeded your monthly budget of ₹" + budget.getTotalLimit())
                            .type(NotificationType.ALERT)
                            .createdAt(LocalDateTime.now())
                            .build());
                } else if (usagePercentage.compareTo(new BigDecimal("80")) >= 0) {
                    notifications.add(NotificationDto.builder()
                            .id("budget_warning_" + budget.getId())
                            .title("Budget Warning")
                            .message("You have used " + usagePercentage.setScale(0, RoundingMode.HALF_UP) + "% of your monthly budget")
                            .type(NotificationType.WARNING)
                            .createdAt(LocalDateTime.now())
                            .build());
                }
            }
        }

        return notifications;
    }
}

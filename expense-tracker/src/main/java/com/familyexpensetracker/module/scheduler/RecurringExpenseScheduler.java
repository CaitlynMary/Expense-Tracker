package com.familyexpensetracker.module.scheduler;

import com.familyexpensetracker.module.expense.entity.Expense;
import com.familyexpensetracker.module.expense.repository.ExpenseRepository;
import com.familyexpensetracker.module.recurring.entity.RecurringExpense;
import com.familyexpensetracker.module.recurring.repository.RecurringExpenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Scheduler that creates {@link Expense} entries from active {@link RecurringExpense} records.
 * Runs daily at midnight (cron: "0 0 0 * * *").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringExpenseScheduler {

    private final RecurringExpenseRepository recurringRepo;
    private final ExpenseRepository expenseRepo;

    /**
     * Executes every day at 00:00 server time.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processRecurringExpenses() {
        log.info("Starting RecurringExpenseScheduler run");
        LocalDate today = LocalDate.now();
        // Fetch active recurring expenses whose next due date is today or before.
        var dueRecurring = recurringRepo.findByIsActiveTrueAndNextDueDateLessThanEqual(today);
        for (RecurringExpense rec : dueRecurring) {
            try {
                // Create a new expense based on the recurring entry.
                Expense expense = Expense.builder()
                        .title(rec.getTitle())
                        .amount(rec.getAmount())
                        .category(rec.getCategory())
                        .paymentMethod(rec.getPaymentMethod())
                        .expenseDate(rec.getNextDueDate())
                        .notes("Generated from recurring expense ID " + rec.getId())
                        .build();
                expenseRepo.save(expense);
                // Update next due date according to frequency.
                LocalDate next = calculateNextDueDate(rec.getNextDueDate(), rec.getFrequency());
                rec.setNextDueDate(next);
                recurringRepo.save(rec);
                log.info("Created expense {} from recurring {} and set next due to {}", expense.getId(), rec.getId(), next);
            } catch (Exception e) {
                log.error("Failed to process recurring expense id {}: {}", rec.getId(), e.getMessage(), e);
            }
        }
        log.info("RecurringExpenseScheduler completed, processed {} items", dueRecurring.size());
    }

    private LocalDate calculateNextDueDate(LocalDate current, RecurringExpense.Frequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}

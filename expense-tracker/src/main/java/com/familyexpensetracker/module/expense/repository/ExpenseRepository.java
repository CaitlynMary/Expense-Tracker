package com.familyexpensetracker.module.expense.repository;

import com.familyexpensetracker.module.expense.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find all expenses ordered by date descending
    Page<Expense> findAllByOrderByExpenseDateDesc(Pageable pageable);

    // Find expenses by category
    Page<Expense> findByCategory(String category, Pageable pageable);

    // Find expenses between dates
    Page<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Find expenses by amount range
    Page<Expense> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    // Search by title containing (case-insensitive)
    Page<Expense> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    boolean existsByTitleIgnoreCaseAndAmountAndCategoryIgnoreCaseAndExpenseDateAndCreatedAtAfter(
            String title,
            BigDecimal amount,
            String category,
            LocalDate expenseDate,
            LocalDateTime createdAtAfter
    );
}

package com.familyexpensetracker.module.recurring.repository;

import com.familyexpensetracker.module.recurring.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    List<RecurringExpense> findByIsActiveTrue();
    List<RecurringExpense> findByIsActiveTrueAndNextDueDateLessThanEqual(LocalDate date);
    boolean existsByTitle(String title);
}

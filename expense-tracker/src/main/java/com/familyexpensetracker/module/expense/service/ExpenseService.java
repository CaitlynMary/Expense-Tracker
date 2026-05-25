package com.familyexpensetracker.module.expense.service;

import com.familyexpensetracker.module.expense.dto.ExpenseRequest;
import com.familyexpensetracker.module.expense.dto.ExpenseResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseService {
    ExpenseResponse addExpense(ExpenseRequest request);
    ExpenseResponse updateExpense(Long id, ExpenseRequest request);
    void deleteExpense(Long id);
    ExpenseResponse getExpenseById(Long id);
    Page<ExpenseResponse> getAllExpenses(int page, int size, String sortBy, String sortDir);
    Page<ExpenseResponse> filterExpensesByCategory(String category, int page, int size, String sortBy, String sortDir);
    Page<ExpenseResponse> filterExpensesByDateRange(LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, String sortDir);
    Page<ExpenseResponse> filterExpensesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, int page, int size, String sortBy, String sortDir);
    Page<ExpenseResponse> searchExpenseByTitle(String title, int page, int size, String sortBy, String sortDir);
}

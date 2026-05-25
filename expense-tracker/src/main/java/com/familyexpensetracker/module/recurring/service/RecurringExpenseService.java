package com.familyexpensetracker.module.recurring.service;

import com.familyexpensetracker.module.recurring.dto.RecurringExpenseRequest;
import com.familyexpensetracker.module.recurring.dto.RecurringExpenseResponse;
import java.util.List;

/**
 * Service contract for managing recurring expenses.
 */
public interface RecurringExpenseService {

    RecurringExpenseResponse createRecurringExpense(RecurringExpenseRequest request);

    RecurringExpenseResponse updateRecurringExpense(Long id, RecurringExpenseRequest request);

    void deleteRecurringExpense(Long id);

    RecurringExpenseResponse getRecurringExpenseById(Long id);

    List<RecurringExpenseResponse> getAllRecurringExpenses();

    List<RecurringExpenseResponse> getActiveRecurringExpenses();

    RecurringExpenseResponse pauseRecurringExpense(Long id);

    RecurringExpenseResponse resumeRecurringExpense(Long id);
}

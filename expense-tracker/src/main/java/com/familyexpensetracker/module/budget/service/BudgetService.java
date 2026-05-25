package com.familyexpensetracker.module.budget.service;

import com.familyexpensetracker.module.budget.dto.BudgetRequest;
import com.familyexpensetracker.module.budget.dto.BudgetResponse;

import java.util.List;

public interface BudgetService {
    BudgetResponse setBudget(BudgetRequest request);
    BudgetResponse updateBudget(Long id, BudgetRequest request);
    void deleteBudget(Long id);
    List<BudgetResponse> getAllBudgets();
    BudgetResponse getBudgetByMonthAndYear(Integer month, Integer year);
}

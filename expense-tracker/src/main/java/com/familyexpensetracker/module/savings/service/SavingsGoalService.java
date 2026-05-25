package com.familyexpensetracker.module.savings.service;

import com.familyexpensetracker.module.savings.dto.SavingsGoalRequest;
import com.familyexpensetracker.module.savings.dto.SavingsGoalResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SavingsGoalService {

    SavingsGoalResponse createSavingsGoal(SavingsGoalRequest request);

    SavingsGoalResponse updateSavingsGoal(Long id, SavingsGoalRequest request);

    void deleteSavingsGoal(Long id);

    SavingsGoalResponse getSavingsGoalById(Long id);

    List<SavingsGoalResponse> getAllSavingsGoals();

    SavingsGoalResponse addSavingsAmount(Long id, BigDecimal amount);
}

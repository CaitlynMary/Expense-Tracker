package com.familyexpensetracker.common.mapper;

import com.familyexpensetracker.module.savings.dto.SavingsGoalRequest;
import com.familyexpensetracker.module.savings.dto.SavingsGoalResponse;
import com.familyexpensetracker.module.savings.entity.SavingsGoal;
import com.familyexpensetracker.module.savings.entity.SavingsGoalStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SavingsGoalMapper {

    public SavingsGoal toEntity(SavingsGoalRequest request) {
        return SavingsGoal.builder()
                .name(request.getName().trim())
                .targetAmount(request.getTargetAmount())
                .savedAmount(request.getSavedAmount())
                .monthlyTarget(request.getMonthlyTarget())
                .targetDate(request.getTargetDate())
                .status(parseStatus(request.getStatus()))
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .build();
    }

    public SavingsGoalResponse toResponse(SavingsGoal goal) {
        BigDecimal targetAmount = goal.getTargetAmount();
        BigDecimal savedAmount = goal.getSavedAmount();
        double progressPercentage = 0.0;

        if (targetAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0 && savedAmount != null) {
            progressPercentage = savedAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .monthlyTarget(goal.getMonthlyTarget())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus() != null ? goal.getStatus().name() : null)
                .notes(goal.getNotes())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .progressPercentage(progressPercentage)
                .completed(goal.getStatus() == SavingsGoalStatus.COMPLETED)
                .build();
    }

    public void updateEntity(SavingsGoal goal, SavingsGoalRequest request) {
        goal.setName(request.getName().trim());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setSavedAmount(request.getSavedAmount());
        goal.setMonthlyTarget(request.getMonthlyTarget());
        goal.setTargetDate(request.getTargetDate());
        goal.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        SavingsGoalStatus status = parseStatus(request.getStatus());
        if (status != null) {
            goal.setStatus(status);
        }
    }

    private SavingsGoalStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return SavingsGoalStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid savings goal status: " + status);
        }
    }
}


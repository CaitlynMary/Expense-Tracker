package com.familyexpensetracker.module.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    
    private Long id;
    private Integer month;
    private Integer year;
    private BigDecimal totalLimit;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;
    private boolean isBudgetExceeded;
    
    @Builder.Default
    private List<CategoryBudgetResponse> categoryBudgets = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBudgetResponse {
        private Long id;
        private String category;
        private BigDecimal limitAmount;
        private BigDecimal spentAmount;
        private BigDecimal remainingAmount;
        private boolean isExceeded;
    }
}

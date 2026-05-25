package com.familyexpensetracker.module.expense.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private String paymentMethod;
    private LocalDate expenseDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.familyexpensetracker.module.recurring.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO used for creating or updating a recurring expense.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount cannot be negative")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotBlank(message = "Frequency is required")
    private String frequency; // e.g., WEEKLY, MONTHLY, YEARLY

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    // End date can be null for indefinite recurring expenses
    private LocalDate endDate;

    @NotNull(message = "Next due date is required")
    @FutureOrPresent(message = "Next due date must be today or in the future")
    private LocalDate nextDueDate;

    // Optional flag; if omitted, service defaults to true (active)
    private Boolean isActive;
}

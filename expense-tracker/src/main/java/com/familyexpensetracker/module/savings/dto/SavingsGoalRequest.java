package com.familyexpensetracker.module.savings.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalRequest {

    @NotBlank(message = "Goal name is required")
    @Size(min = 2, max = 100, message = "Goal name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    private BigDecimal targetAmount;

    @NotNull(message = "Saved amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Saved amount cannot be negative")
    private BigDecimal savedAmount;

    @NotNull(message = "Monthly target is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Monthly target cannot be negative")
    private BigDecimal monthlyTarget;

    @NotNull(message = "Target date is required")
    @FutureOrPresent(message = "Target date must be today or in the future")
    private LocalDate targetDate;

    // Optional status – if omitted defaults to ACTIVE in service
    private String status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}

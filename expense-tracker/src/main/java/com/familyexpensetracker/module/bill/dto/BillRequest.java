package com.familyexpensetracker.module.bill.dto;

import com.familyexpensetracker.module.bill.entity.BillFrequency;
import com.familyexpensetracker.module.bill.entity.BillStatus;
import com.familyexpensetracker.module.bill.entity.BillType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class BillRequest {

    @NotBlank(message = "Bill name is required")
    @Size(min = 2, max = 100, message = "Bill name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private LocalDate paidDate;

    private BillStatus status;

    private BillType billType;

    @NotNull(message = "Frequency is required")
    private BillFrequency frequency;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}

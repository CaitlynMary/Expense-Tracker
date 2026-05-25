package com.familyexpensetracker.module.paymentmethod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a payment method.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {

    @NotBlank(message = "Payment method name must not be blank")
    @Size(min = 2, max = 50, message = "Payment method name must be between 2 and 50 characters")
    private String methodName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}

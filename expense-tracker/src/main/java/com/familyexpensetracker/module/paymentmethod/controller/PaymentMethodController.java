package com.familyexpensetracker.module.paymentmethod.controller;

import com.familyexpensetracker.common.ApiResponse;
import com.familyexpensetracker.module.paymentmethod.dto.PaymentMethodRequest;
import com.familyexpensetracker.module.paymentmethod.dto.PaymentMethodResponse;
import com.familyexpensetracker.module.paymentmethod.service.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Methods", description = "APIs for managing expense payment methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @Operation(summary = "Create a payment method")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> createPaymentMethod(
            @Valid @RequestBody PaymentMethodRequest request) {

        PaymentMethodResponse paymentMethod = paymentMethodService.createPaymentMethod(request);

        ApiResponse<PaymentMethodResponse> response = ApiResponse.<PaymentMethodResponse>builder()
                .success(true)
                .message("Payment method created successfully")
                .data(paymentMethod)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all active payment methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getAllPaymentMethods() {

        List<PaymentMethodResponse> paymentMethods = paymentMethodService.getAllPaymentMethods();

        ApiResponse<List<PaymentMethodResponse>> response = ApiResponse.<List<PaymentMethodResponse>>builder()
                .success(true)
                .message("Payment methods retrieved successfully")
                .data(paymentMethods)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment method by id")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethodById(@PathVariable Long id) {

        PaymentMethodResponse paymentMethod = paymentMethodService.getPaymentMethodById(id);

        ApiResponse<PaymentMethodResponse> response = ApiResponse.<PaymentMethodResponse>builder()
                .success(true)
                .message("Payment method retrieved successfully")
                .data(paymentMethod)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a payment method")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> updatePaymentMethod(
            @PathVariable Long id,
            @Valid @RequestBody PaymentMethodRequest request) {

        PaymentMethodResponse paymentMethod = paymentMethodService.updatePaymentMethod(id, request);

        ApiResponse<PaymentMethodResponse> response = ApiResponse.<PaymentMethodResponse>builder()
                .success(true)
                .message("Payment method updated successfully")
                .data(paymentMethod)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Disable a payment method")
    public ResponseEntity<ApiResponse<Void>> disablePaymentMethod(@PathVariable Long id) {

        paymentMethodService.disablePaymentMethod(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Payment method disabled successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search payment methods by keyword")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> searchPaymentMethods(
            @RequestParam String keyword) {

        List<PaymentMethodResponse> paymentMethods = paymentMethodService.searchPaymentMethods(keyword);

        ApiResponse<List<PaymentMethodResponse>> response = ApiResponse.<List<PaymentMethodResponse>>builder()
                .success(true)
                .message("Payment methods retrieved successfully")
                .data(paymentMethods)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}

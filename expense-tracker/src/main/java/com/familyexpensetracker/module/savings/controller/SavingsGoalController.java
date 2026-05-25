package com.familyexpensetracker.module.savings.controller;

import com.familyexpensetracker.common.ApiResponse;
import com.familyexpensetracker.module.savings.dto.SavingsGoalRequest;
import com.familyexpensetracker.module.savings.dto.SavingsGoalResponse;
import com.familyexpensetracker.module.savings.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> createSavingsGoal(
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.createSavingsGoal(request);
        ApiResponse<SavingsGoalResponse> apiResponse = ApiResponse.<SavingsGoalResponse>builder()
                .success(true)
                .message("Savings goal created successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> updateSavingsGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.updateSavingsGoal(id, request);
        ApiResponse<SavingsGoalResponse> apiResponse = ApiResponse.<SavingsGoalResponse>builder()
                .success(true)
                .message("Savings goal updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<Void>> deleteSavingsGoal(@PathVariable Long id) {
        savingsGoalService.deleteSavingsGoal(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .message("Savings goal deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<List<SavingsGoalResponse>>> getAllSavingsGoals() {
        List<SavingsGoalResponse> list = savingsGoalService.getAllSavingsGoals();
        ApiResponse<List<SavingsGoalResponse>> apiResponse = ApiResponse.<List<SavingsGoalResponse>>builder()
                .success(true)
                .message("Savings goals retrieved successfully")
                .data(list)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> getSavingsGoalById(@PathVariable Long id) {
        SavingsGoalResponse response = savingsGoalService.getSavingsGoalById(id);
        ApiResponse<SavingsGoalResponse> apiResponse = ApiResponse.<SavingsGoalResponse>builder()
                .success(true)
                .message("Savings goal retrieved successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}

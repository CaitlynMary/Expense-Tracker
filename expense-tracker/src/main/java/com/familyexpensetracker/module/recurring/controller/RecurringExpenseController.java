package com.familyexpensetracker.module.recurring.controller;

import com.familyexpensetracker.common.ApiResponse;
import com.familyexpensetracker.module.recurring.dto.RecurringExpenseRequest;
import com.familyexpensetracker.module.recurring.dto.RecurringExpenseResponse;
import com.familyexpensetracker.module.recurring.service.RecurringExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller that manages recurring expenses.
 *
 * All endpoints are protected by JWT – the `ROLE_USER` authority is required.
 * The controller follows the same pattern used throughout the project
 * (ApiResponse wrapper, Bean Validation, clean‑architecture separation).
 */
@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
@Validated                     // enables method‑level @Valid on request bodies
public class RecurringExpenseController {

    private final RecurringExpenseService service;

    /* --------------------------------------------------------------------- *
     * CREATE
     * --------------------------------------------------------------------- */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> createRecurring(
            @Valid @RequestBody RecurringExpenseRequest request) {

        RecurringExpenseResponse created = service.createRecurringExpense(request);
        ApiResponse<RecurringExpenseResponse> api = ApiResponse.<RecurringExpenseResponse>builder()
                .success(true)
                .message("Recurring expense created")
                .data(created)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(api, HttpStatus.CREATED);
    }

    /* --------------------------------------------------------------------- *
     * UPDATE
     * --------------------------------------------------------------------- */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> updateRecurring(
            @PathVariable Long id,
            @Valid @RequestBody RecurringExpenseRequest request) {

        RecurringExpenseResponse updated = service.updateRecurringExpense(id, request);
        ApiResponse<RecurringExpenseResponse> api = ApiResponse.<RecurringExpenseResponse>builder()
                .success(true)
                .message("Recurring expense updated")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(api);
    }

    /* --------------------------------------------------------------------- *
     * DELETE
     * --------------------------------------------------------------------- */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<Void>> deleteRecurring(@PathVariable Long id) {
        service.deleteRecurringExpense(id);
        ApiResponse<Void> api = ApiResponse.<Void>builder()
                .success(true)
                .message("Recurring expense deleted")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(api);
    }

    /* --------------------------------------------------------------------- *
     * PAUSE
     * --------------------------------------------------------------------- */
    @PutMapping("/{id}/pause")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> pauseRecurring(@PathVariable Long id) {
        RecurringExpenseResponse paused = service.pauseRecurringExpense(id);
        ApiResponse<RecurringExpenseResponse> api = ApiResponse.<RecurringExpenseResponse>builder()
                .success(true)
                .message("Recurring expense paused")
                .data(paused)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(api);
    }

    /* --------------------------------------------------------------------- *
     * RESUME
     * --------------------------------------------------------------------- */
    @PutMapping("/{id}/resume")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> resumeRecurring(@PathVariable Long id) {
        RecurringExpenseResponse resumed = service.resumeRecurringExpense(id);
        ApiResponse<RecurringExpenseResponse> api = ApiResponse.<RecurringExpenseResponse>builder()
                .success(true)
                .message("Recurring expense resumed")
                .data(resumed)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(api);
    }

    /* --------------------------------------------------------------------- *
     * READ – list all (optionally filter by activity)
     * --------------------------------------------------------------------- */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<List<RecurringExpenseResponse>>> getAll(
            @RequestParam(required = false) Boolean active) {

        List<RecurringExpenseResponse> list;
        if (active == null) {
            // no filter – return everything
            list = service.getAllRecurringExpenses();
        } else if (active) {
            // only active recurring expenses
            list = service.getActiveRecurringExpenses();
        } else {
            // inactive only – derive from the full list
            list = service.getAllRecurringExpenses()
                    .stream()
                    .filter(r -> Boolean.FALSE.equals(r.getIsActive()))
                    .toList();
        }

        ApiResponse<List<RecurringExpenseResponse>> api = ApiResponse.<List<RecurringExpenseResponse>>builder()
                .success(true)
                .message("Recurring expenses retrieved")
                .data(list)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(api);
    }
}
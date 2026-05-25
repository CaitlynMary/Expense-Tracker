package com.familyexpensetracker.module.budget.controller;

import com.familyexpensetracker.module.budget.dto.BudgetRequest;
import com.familyexpensetracker.module.budget.dto.BudgetResponse;
import com.familyexpensetracker.module.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> setBudget(@Valid @RequestBody BudgetRequest request) {
        return new ResponseEntity<>(budgetService.setBudget(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{month}/{year}")
    public ResponseEntity<BudgetResponse> getBudgetByMonthAndYear(
            @PathVariable Integer month,
            @PathVariable Integer year) {
        return ResponseEntity.ok(budgetService.getBudgetByMonthAndYear(month, year));
    }
}

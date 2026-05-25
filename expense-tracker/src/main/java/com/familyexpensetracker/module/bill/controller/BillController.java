package com.familyexpensetracker.module.bill.controller;

import com.familyexpensetracker.common.ApiResponse;
import com.familyexpensetracker.module.bill.dto.BillRequest;
import com.familyexpensetracker.module.bill.dto.BillResponse;
import com.familyexpensetracker.module.bill.service.BillService;
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
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> createBill(
            @Valid @RequestBody BillRequest request) {

        BillResponse bill = billService.createBill(request);

        ApiResponse<BillResponse> response = ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill created successfully")
                .data(bill)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> updateBill(
            @PathVariable Long id,
            @Valid @RequestBody BillRequest request) {

        BillResponse bill = billService.updateBill(id, request);

        ApiResponse<BillResponse> response = ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill updated successfully")
                .data(bill)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBill(@PathVariable Long id) {

        billService.deleteBill(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Bill deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<ApiResponse<BillResponse>> markBillAsPaid(@PathVariable Long id) {

        BillResponse bill = billService.markBillAsPaid(id);

        ApiResponse<BillResponse> response = ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill marked as paid successfully")
                .data(bill)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAllBills() {

        List<BillResponse> bills = billService.getAllBills();

        ApiResponse<List<BillResponse>> response = ApiResponse.<List<BillResponse>>builder()
                .success(true)
                .message("Bills retrieved successfully")
                .data(bills)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable Long id) {

        BillResponse bill = billService.getBillById(id);

        ApiResponse<BillResponse> response = ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill retrieved successfully")
                .data(bill)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getUpcomingBills() {

        List<BillResponse> bills = billService.getUpcomingBills();

        ApiResponse<List<BillResponse>> response = ApiResponse.<List<BillResponse>>builder()
                .success(true)
                .message("Upcoming bills retrieved successfully")
                .data(bills)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getOverdueBills() {

        List<BillResponse> bills = billService.getOverdueBills();

        ApiResponse<List<BillResponse>> response = ApiResponse.<List<BillResponse>>builder()
                .success(true)
                .message("Overdue bills retrieved successfully")
                .data(bills)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}

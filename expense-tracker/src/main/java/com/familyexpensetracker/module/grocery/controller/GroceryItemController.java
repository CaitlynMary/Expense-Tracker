package com.familyexpensetracker.module.grocery.controller;

import com.familyexpensetracker.common.ApiResponse;
import com.familyexpensetracker.module.grocery.dto.GroceryItemRequest;
import com.familyexpensetracker.module.grocery.dto.GroceryItemResponse;
import com.familyexpensetracker.module.grocery.service.GroceryItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/groceries")
@RequiredArgsConstructor
public class GroceryItemController {

    private final GroceryItemService groceryItemService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<GroceryItemResponse>> addItem(@Valid @RequestBody GroceryItemRequest request) {
        GroceryItemResponse response = groceryItemService.addItem(request);
        ApiResponse<GroceryItemResponse> apiResponse = ApiResponse.<GroceryItemResponse>builder()
                .success(true)
                .message("Grocery item created successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<GroceryItemResponse>> updateItem(@PathVariable Long id,
                                                                        @Valid @RequestBody GroceryItemRequest request) {
        GroceryItemResponse response = groceryItemService.updateItem(id, request);
        ApiResponse<GroceryItemResponse> apiResponse = ApiResponse.<GroceryItemResponse>builder()
                .success(true)
                .message("Grocery item updated successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        groceryItemService.deleteItem(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .message("Grocery item deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}/purchase")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<GroceryItemResponse>> purchaseItem(@PathVariable Long id) {
        GroceryItemResponse response = groceryItemService.markAsPurchased(id);
        ApiResponse<GroceryItemResponse> apiResponse = ApiResponse.<GroceryItemResponse>builder()
                .success(true)
                .message("Grocery item marked as purchased")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<List<GroceryItemResponse>>> getPendingItems() {
        List<GroceryItemResponse> items = groceryItemService.getPendingItems();
        ApiResponse<List<GroceryItemResponse>> apiResponse = ApiResponse.<List<GroceryItemResponse>>builder()
                .success(true)
                .message("Pending grocery items retrieved successfully")
                .data(items)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<List<GroceryItemResponse>>> getPurchaseHistory() {
        List<GroceryItemResponse> items = groceryItemService.getPurchasedItems();
        ApiResponse<List<GroceryItemResponse>> apiResponse = ApiResponse.<List<GroceryItemResponse>>builder()
                .success(true)
                .message("Purchased grocery items retrieved successfully")
                .data(items)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}

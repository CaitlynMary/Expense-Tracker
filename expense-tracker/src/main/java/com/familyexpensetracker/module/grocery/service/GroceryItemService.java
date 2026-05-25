package com.familyexpensetracker.module.grocery.service;

import com.familyexpensetracker.module.grocery.dto.GroceryItemRequest;
import com.familyexpensetracker.module.grocery.dto.GroceryItemResponse;
import java.util.List;

public interface GroceryItemService {
    GroceryItemResponse addItem(GroceryItemRequest request);
    GroceryItemResponse updateItem(Long id, GroceryItemRequest request);
    void deleteItem(Long id);
    GroceryItemResponse markAsPurchased(Long id);
    List<GroceryItemResponse> getPendingItems();
    List<GroceryItemResponse> getPurchasedItems();
}

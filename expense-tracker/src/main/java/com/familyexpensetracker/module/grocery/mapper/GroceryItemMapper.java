package com.familyexpensetracker.module.grocery.mapper;

import com.familyexpensetracker.module.grocery.dto.GroceryItemRequest;
import com.familyexpensetracker.module.grocery.dto.GroceryItemResponse;
import com.familyexpensetracker.module.grocery.entity.GroceryItem;
import com.familyexpensetracker.module.grocery.entity.GroceryItemStatus;

public class GroceryItemMapper {

    public static GroceryItem toEntity(GroceryItemRequest request) {
        return GroceryItem.builder()
                .name(request.getName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .estimatedPrice(request.getEstimatedPrice())
                .notes(request.getNotes())
                .isPurchased(false)
                .status(GroceryItemStatus.PENDING)
                .build();
    }

    public static GroceryItemResponse toResponse(GroceryItem entity) {
        GroceryItemResponse response = new GroceryItemResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setQuantity(entity.getQuantity());
        response.setUnit(entity.getUnit());
        response.setEstimatedPrice(entity.getEstimatedPrice());
        response.setIsPurchased(entity.getIsPurchased());
        response.setStatus(entity.getStatus() == null ? GroceryItemStatus.PENDING.name() : entity.getStatus().name());
        response.setPurchasedAt(entity.getPurchasedAt());
        response.setNotes(entity.getNotes());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

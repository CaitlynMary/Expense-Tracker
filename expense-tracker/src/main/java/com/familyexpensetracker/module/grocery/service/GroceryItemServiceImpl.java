package com.familyexpensetracker.module.grocery.service;

import com.familyexpensetracker.exception.DuplicateResourceException;
import com.familyexpensetracker.exception.ResourceNotFoundException;
import com.familyexpensetracker.module.grocery.dto.GroceryItemRequest;
import com.familyexpensetracker.module.grocery.dto.GroceryItemResponse;
import com.familyexpensetracker.module.grocery.entity.GroceryItem;
import com.familyexpensetracker.module.grocery.entity.GroceryItemStatus;
import com.familyexpensetracker.module.grocery.mapper.GroceryItemMapper;
import com.familyexpensetracker.module.grocery.repository.GroceryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroceryItemServiceImpl implements GroceryItemService {

    private final GroceryItemRepository groceryItemRepository;

    @Override
    @Transactional
    public GroceryItemResponse addItem(GroceryItemRequest request) {
        groceryItemRepository.findByNameAndStatus(request.getName(), GroceryItemStatus.PENDING)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Pending grocery item with name '" + request.getName() + "' already exists");
                });
        GroceryItem entity = GroceryItemMapper.toEntity(request);
        GroceryItem saved = groceryItemRepository.save(entity);
        return GroceryItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GroceryItemResponse updateItem(Long id, GroceryItemRequest request) {
        GroceryItem existing = groceryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found with id " + id));
        // If name is being changed, ensure no duplicate
        if (!existing.getName().equals(request.getName())) {
            groceryItemRepository.findByNameAndStatus(request.getName(), GroceryItemStatus.PENDING)
                    .ifPresent(conflict -> {
                        if (!conflict.getId().equals(id)) {
                            throw new DuplicateResourceException("Pending grocery item with name '" + request.getName() + "' already exists");
                        }
                    });
        }
        existing.setName(request.getName());
        existing.setQuantity(request.getQuantity());
        existing.setUnit(request.getUnit());
        existing.setEstimatedPrice(request.getEstimatedPrice());
        existing.setNotes(request.getNotes());
        GroceryItem saved = groceryItemRepository.save(existing);
        return GroceryItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        GroceryItem existing = groceryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found with id " + id));
        groceryItemRepository.delete(existing);
    }

    @Override
    @Transactional
    public GroceryItemResponse markAsPurchased(Long id) {
        GroceryItem existing = groceryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found with id " + id));
        existing.setIsPurchased(true);
        existing.setStatus(GroceryItemStatus.PURCHASED);
        existing.setPurchasedAt(LocalDateTime.now());
        GroceryItem saved = groceryItemRepository.save(existing);
        return GroceryItemMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroceryItemResponse> getPendingItems() {
        return groceryItemRepository.findPendingItems()
                .stream()
                .map(GroceryItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroceryItemResponse> getPurchasedItems() {
        return groceryItemRepository.findPurchasedItems()
                .stream()
                .map(GroceryItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}

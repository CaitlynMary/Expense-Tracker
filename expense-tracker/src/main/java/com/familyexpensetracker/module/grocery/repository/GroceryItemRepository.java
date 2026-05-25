package com.familyexpensetracker.module.grocery.repository;

import com.familyexpensetracker.module.grocery.entity.GroceryItem;
import com.familyexpensetracker.module.grocery.entity.GroceryItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItem, Long> {
    List<GroceryItem> findByIsPurchased(Boolean isPurchased);
    List<GroceryItem> findByStatus(GroceryItemStatus status);
    Optional<GroceryItem> findByName(String name);
    Optional<GroceryItem> findByNameAndStatus(String name, GroceryItemStatus status);

    @Query("select item from GroceryItem item where item.status = com.familyexpensetracker.module.grocery.entity.GroceryItemStatus.PENDING or (item.status is null and item.isPurchased = false)")
    List<GroceryItem> findPendingItems();

    @Query("select item from GroceryItem item where item.status = com.familyexpensetracker.module.grocery.entity.GroceryItemStatus.PURCHASED or item.isPurchased = true")
    List<GroceryItem> findPurchasedItems();
}

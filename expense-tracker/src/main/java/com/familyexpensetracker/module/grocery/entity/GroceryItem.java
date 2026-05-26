package com.familyexpensetracker.module.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grocery_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroceryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false)
    private String unit;

    @Column(name = "estimated_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedPrice;

    @Builder.Default
    @Column(name = "is_purchased", nullable = false)
    private Boolean isPurchased = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroceryItemStatus status = GroceryItemStatus.PENDING;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void setDefaults() {
        if (status == null) {
            status = GroceryItemStatus.PENDING;
        }
        if (isPurchased == null) {
            isPurchased = status == GroceryItemStatus.PURCHASED;
        }
    }
}

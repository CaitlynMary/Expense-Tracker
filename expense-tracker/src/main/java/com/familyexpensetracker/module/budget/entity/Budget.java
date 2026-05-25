package com.familyexpensetracker.module.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"month", "year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Column(nullable = false)
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be valid")
    @Column(nullable = false)
    private Integer year;

    @NotNull(message = "Total limit is required")
    @DecimalMin(value = "0.01", message = "Total limit must be greater than zero")
    @Column(name = "total_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLimit;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CategoryBudget> categoryBudgets = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper method to keep both sides of the bi-directional relationship in sync
    public void addCategoryBudget(CategoryBudget categoryBudget) {
        categoryBudgets.add(categoryBudget);
        categoryBudget.setBudget(this);
    }

    public void removeCategoryBudget(CategoryBudget categoryBudget) {
        categoryBudgets.remove(categoryBudget);
        categoryBudget.setBudget(null);
    }
}

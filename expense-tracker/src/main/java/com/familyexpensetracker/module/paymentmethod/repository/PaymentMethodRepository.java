package com.familyexpensetracker.module.paymentmethod.repository;

import com.familyexpensetracker.module.paymentmethod.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link PaymentMethod} entity.
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /**
     * Check if a payment method with the given name already exists (case-insensitive).
     */
    boolean existsByMethodNameIgnoreCase(String methodName);

    /**
     * Find all active payment methods.
     */
    List<PaymentMethod> findByIsActiveTrue();

    /**
     * Search payment methods by name keyword (case-insensitive).
     */
    List<PaymentMethod> findByMethodNameContainingIgnoreCase(String keyword);
}

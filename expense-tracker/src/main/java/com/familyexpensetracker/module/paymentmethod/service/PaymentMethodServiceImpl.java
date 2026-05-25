package com.familyexpensetracker.module.paymentmethod.service;

import com.familyexpensetracker.exception.BadRequestException;
import com.familyexpensetracker.exception.DuplicateResourceException;
import com.familyexpensetracker.exception.ResourceNotFoundException;
import com.familyexpensetracker.module.paymentmethod.dto.PaymentMethodRequest;
import com.familyexpensetracker.module.paymentmethod.dto.PaymentMethodResponse;
import com.familyexpensetracker.module.paymentmethod.entity.PaymentMethod;
import com.familyexpensetracker.module.paymentmethod.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    @Transactional
    public PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request) {
        String methodName = normalizeMethodName(request.getMethodName());

        if (paymentMethodRepository.existsByMethodNameIgnoreCase(methodName)) {
            throw new DuplicateResourceException("Payment method", "methodName", methodName);
        }

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .methodName(methodName)
                .description(normalizeDescription(request.getDescription()))
                .isActive(true)
                .build();

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        return toResponse(savedPaymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getAllPaymentMethods() {
        return paymentMethodRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse getPaymentMethodById(Long id) {
        PaymentMethod paymentMethod = findPaymentMethodById(id);
        return toResponse(paymentMethod);
    }

    @Override
    @Transactional
    public PaymentMethodResponse updatePaymentMethod(Long id, PaymentMethodRequest request) {
        PaymentMethod paymentMethod = findPaymentMethodById(id);
        String methodName = normalizeMethodName(request.getMethodName());

        if (!paymentMethod.getMethodName().equalsIgnoreCase(methodName)
                && paymentMethodRepository.existsByMethodNameIgnoreCase(methodName)) {
            throw new DuplicateResourceException("Payment method", "methodName", methodName);
        }

        paymentMethod.setMethodName(methodName);
        paymentMethod.setDescription(normalizeDescription(request.getDescription()));

        PaymentMethod updatedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        return toResponse(updatedPaymentMethod);
    }

    @Override
    @Transactional
    public void disablePaymentMethod(Long id) {
        PaymentMethod paymentMethod = findPaymentMethodById(id);
        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> searchPaymentMethods(String keyword) {
        if (keyword == null || keyword.trim().isBlank()) {
            throw new BadRequestException("Search keyword must not be blank");
        }

        return paymentMethodRepository.findByMethodNameContainingIgnoreCase(keyword.trim())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PaymentMethod findPaymentMethodById(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method", "id", id));
    }

    private PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .methodName(paymentMethod.getMethodName())
                .description(paymentMethod.getDescription())
                .isActive(paymentMethod.getIsActive())
                .createdAt(paymentMethod.getCreatedAt())
                .updatedAt(paymentMethod.getUpdatedAt())
                .build();
    }

    private String normalizeMethodName(String methodName) {
        return methodName.trim();
    }

    private String normalizeDescription(String description) {
        return description == null || description.trim().isBlank() ? null : description.trim();
    }
}

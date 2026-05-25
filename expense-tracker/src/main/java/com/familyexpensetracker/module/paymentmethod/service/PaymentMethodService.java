package com.familyexpensetracker.module.paymentmethod.service;

import com.familyexpensetracker.module.paymentmethod.dto.PaymentMethodRequest;
import com.familyexpensetracker.module.paymentmethod.dto.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {

    PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request);

    List<PaymentMethodResponse> getAllPaymentMethods();

    PaymentMethodResponse getPaymentMethodById(Long id);

    PaymentMethodResponse updatePaymentMethod(Long id, PaymentMethodRequest request);

    void disablePaymentMethod(Long id);

    List<PaymentMethodResponse> searchPaymentMethods(String keyword);
}

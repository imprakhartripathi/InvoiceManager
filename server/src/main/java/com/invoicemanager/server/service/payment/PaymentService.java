package com.invoicemanager.server.service.payment;

import com.invoicemanager.server.dto.payment.CreateOrderResponse;

public interface PaymentService {
    CreateOrderResponse createOrder(String userId, String invoiceId);

    CreateOrderResponse createPublicOrder(String invoiceId);

    void handleWebhook(String payloadJson, String signature);
}

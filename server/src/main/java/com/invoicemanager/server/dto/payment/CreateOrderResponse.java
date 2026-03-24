package com.invoicemanager.server.dto.payment;

public record CreateOrderResponse(String orderId, String keyId, Long amountInPaise, String currency) {
}

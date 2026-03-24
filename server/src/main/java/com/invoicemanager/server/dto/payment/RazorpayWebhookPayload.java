package com.invoicemanager.server.dto.payment;

public record RazorpayWebhookPayload(String event, Payload payload) {

    public record Payload(PaymentEntity payment) {
    }

    public record PaymentEntity(String id, String order_id, String status) {
    }
}

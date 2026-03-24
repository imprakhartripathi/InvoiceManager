package com.invoicemanager.server.dto.invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record InvoiceResponse(
        String id,
        String userId,
        String templateId,
        Map<String, Object> data,
        List<Map<String, Object>> lineItems,
        BigDecimal total,
        String status,
        String razorpayOrderId,
        Instant createdAt,
        Instant paidAt) {
}

package com.invoicemanager.server.dto.invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record InvoiceResponse(
        String id,
        String userId,
        String templateId,
        String displayName,
        Map<String, Object> data,
        List<Map<String, Object>> lineItems,
        BigDecimal total,
        String status,
        String razorpayOrderId,
        String paidVia,
        String paidBy,
        Instant createdAt,
        Instant paidAt) {
}

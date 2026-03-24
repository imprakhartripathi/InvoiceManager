package com.invoicemanager.server.dto.invoice;

import java.math.BigDecimal;

public record InvoiceSummaryResponse(
        String invoiceId,
        String templateName,
        BigDecimal total,
        String status,
        String razorpayOrderId) {
}

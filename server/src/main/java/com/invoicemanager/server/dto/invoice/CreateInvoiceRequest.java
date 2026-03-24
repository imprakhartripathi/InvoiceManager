package com.invoicemanager.server.dto.invoice;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInvoiceRequest(
        @NotBlank String templateId,
        @NotNull Map<String, Object> data,
        List<Map<String, Object>> lineItems) {
}

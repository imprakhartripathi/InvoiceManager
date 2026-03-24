package com.invoicemanager.server.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(@NotBlank String invoiceId) {
}

package com.invoicemanager.server.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateRequest(
        @NotBlank String fullName,
        String businessName,
        @NotBlank String defaultDisplayName,
        String newCustomDisplayName) {
}

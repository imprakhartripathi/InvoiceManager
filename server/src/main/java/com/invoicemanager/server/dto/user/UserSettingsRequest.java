package com.invoicemanager.server.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UserSettingsRequest(
        String host,
        @Min(1) @Max(65535) Integer port,
        @Email String email,
        String appPassword,
        boolean enabled) {
}

package com.invoicemanager.server.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UserSettingsRequest(
        @NotBlank String host,
        @Min(1) @Max(65535) Integer port,
        @NotBlank @Email String email,
        @NotBlank String appPassword,
        boolean enabled) {
}

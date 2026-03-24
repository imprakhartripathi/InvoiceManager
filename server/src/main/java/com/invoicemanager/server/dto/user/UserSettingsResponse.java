package com.invoicemanager.server.dto.user;

public record UserSettingsResponse(
        String host,
        Integer port,
        String email,
        boolean enabled) {
}

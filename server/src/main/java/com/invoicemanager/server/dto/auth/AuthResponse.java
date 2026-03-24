package com.invoicemanager.server.dto.auth;

public record AuthResponse(String token, String userId, String email) {
}

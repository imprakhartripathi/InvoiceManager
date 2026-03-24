package com.invoicemanager.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank String fullName,
        String businessName,
        @NotBlank @Size(min = 8, max = 72) String password) {
}

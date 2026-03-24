package com.invoicemanager.server.service.auth;

import com.invoicemanager.server.dto.auth.AuthResponse;
import com.invoicemanager.server.dto.auth.LoginRequest;
import com.invoicemanager.server.dto.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}

package com.invoicemanager.server.service.auth;

import java.util.Locale;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.invoicemanager.server.dto.auth.AuthResponse;
import com.invoicemanager.server.dto.auth.LoginRequest;
import com.invoicemanager.server.dto.auth.SignupRequest;
import com.invoicemanager.server.exception.UnauthorizedException;
import com.invoicemanager.server.exception.ValidationException;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.repository.UserRepository;
import com.invoicemanager.server.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse signup(SignupRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        String normalizedFullName = request.fullName().trim();
        String normalizedBusinessName = request.businessName() == null ? "" : request.businessName().trim();
        String defaultDisplayName = normalizedBusinessName.isBlank() ? normalizedFullName : normalizedBusinessName;
        log.info("Signup attempt for email={}", normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Signup blocked: email already registered email={}", normalizedEmail);
            throw new ValidationException("Email is already registered");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .fullName(normalizedFullName)
                .businessName(normalizedBusinessName)
                .defaultDisplayName(defaultDisplayName)
                .savedCustomDisplayNames(new ArrayList<>())
                .build();

        User saved = userRepository.save(user);
        log.info("Signup success for userId={} email={}", saved.getId(), saved.getEmail());
        String token = jwtService.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, saved.getId(), saved.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}

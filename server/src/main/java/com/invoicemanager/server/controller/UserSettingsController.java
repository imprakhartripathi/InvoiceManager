package com.invoicemanager.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invoicemanager.server.dto.user.UserSettingsRequest;
import com.invoicemanager.server.dto.user.UserSettingsResponse;
import com.invoicemanager.server.security.UserPrincipal;
import com.invoicemanager.server.service.user.UserSettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user-settings")
@Validated
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @PutMapping
    public ResponseEntity<UserSettingsResponse> upsert(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserSettingsRequest request) {
        return ResponseEntity.ok(userSettingsService.upsertSettings(principal.userId(), request));
    }

    @GetMapping
    public ResponseEntity<UserSettingsResponse> get(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userSettingsService.getSettings(principal.userId()));
    }
}

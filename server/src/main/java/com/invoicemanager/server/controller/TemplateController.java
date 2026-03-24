package com.invoicemanager.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invoicemanager.server.dto.template.TemplateRequest;
import com.invoicemanager.server.dto.template.TemplateResponse;
import com.invoicemanager.server.security.UserPrincipal;
import com.invoicemanager.server.service.template.TemplateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/templates")
@Validated
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateResponse> create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.ok(templateService.create(principal.userId(), request));
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(templateService.getMyTemplates(principal.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getOne(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return ResponseEntity.ok(templateService.getOne(principal.userId(), id));
    }
}

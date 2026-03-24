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

import com.invoicemanager.server.dto.invoice.CreateInvoiceRequest;
import com.invoicemanager.server.dto.invoice.InvoiceResponse;
import com.invoicemanager.server.dto.invoice.InvoiceSummaryResponse;
import com.invoicemanager.server.security.UserPrincipal;
import com.invoicemanager.server.service.invoice.InvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/api/invoices")
    public ResponseEntity<InvoiceResponse> create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.createInvoice(principal.userId(), request));
    }

    @GetMapping("/api/invoices")
    public ResponseEntity<List<InvoiceResponse>> getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(invoiceService.getMyInvoices(principal.userId()));
    }

    @GetMapping("/api/invoices/{id}")
    public ResponseEntity<InvoiceResponse> getOne(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return ResponseEntity.ok(invoiceService.getInvoice(principal.userId(), id));
    }

    @GetMapping("/api/public/invoices/{id}/summary")
    public ResponseEntity<InvoiceSummaryResponse> getPublicSummary(@PathVariable String id) {
        return ResponseEntity.ok(invoiceService.getPublicInvoiceSummary(id));
    }
}

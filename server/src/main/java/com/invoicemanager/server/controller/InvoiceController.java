package com.invoicemanager.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.invoicemanager.server.dto.invoice.CreateInvoiceRequest;
import com.invoicemanager.server.dto.invoice.InvoiceResponse;
import com.invoicemanager.server.dto.invoice.InvoiceSummaryResponse;
import com.invoicemanager.server.dto.invoice.UpdateInvoiceRequest;
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

    @PutMapping("/api/invoices/{id}")
    public ResponseEntity<InvoiceResponse> update(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.updateInvoice(principal.userId(), id, request));
    }

    @DeleteMapping("/api/invoices/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        invoiceService.deleteInvoice(principal.userId(), id);
        return ResponseEntity.noContent().build();
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

    @PostMapping("/api/invoices/{id}/send")
    public ResponseEntity<InvoiceResponse> markAsSent(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return ResponseEntity.ok(invoiceService.markAsSent(principal.userId(), id));
    }

    @PostMapping("/api/invoices/{id}/pay/cash")
    public ResponseEntity<InvoiceResponse> markAsPaidCash(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return ResponseEntity.ok(invoiceService.markAsPaidCash(principal.userId(), id));
    }
}

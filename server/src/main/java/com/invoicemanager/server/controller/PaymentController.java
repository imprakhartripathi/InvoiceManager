package com.invoicemanager.server.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invoicemanager.server.dto.payment.CreateOrderRequest;
import com.invoicemanager.server.dto.payment.CreateOrderResponse;
import com.invoicemanager.server.security.UserPrincipal;
import com.invoicemanager.server.service.payment.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@Validated
@RequiredArgsConstructor
public class PaymentController {

    private static final String RAZORPAY_SIGNATURE_HEADER = "X-Razorpay-Signature";

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(principal.userId(), request.invoiceId()));
    }

    @PostMapping("/public/orders")
    public ResponseEntity<CreateOrderResponse> createPublicOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createPublicOrder(request.invoiceId()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
            @RequestHeader(name = RAZORPAY_SIGNATURE_HEADER, required = false) String signature,
            @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        paymentService.handleWebhook(payload, signature == null ? "" : signature);
        return ResponseEntity.ok().build();
    }
}

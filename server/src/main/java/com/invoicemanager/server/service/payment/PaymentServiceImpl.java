package com.invoicemanager.server.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoicemanager.server.config.RazorpayConfig;
import com.invoicemanager.server.dto.payment.CreateOrderResponse;
import com.invoicemanager.server.dto.payment.RazorpayWebhookPayload;
import com.invoicemanager.server.exception.PaymentVerificationException;
import com.invoicemanager.server.exception.ResourceNotFoundException;
import com.invoicemanager.server.exception.ValidationException;
import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.InvoiceStatus;
import com.invoicemanager.server.repository.InvoiceRepository;
import com.invoicemanager.server.service.invoice.InvoiceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final RazorpayConfig razorpayConfig;
    private final ObjectMapper objectMapper;

    @Override
    public CreateOrderResponse createOrder(String userId, String invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return createAndAttachOrder(invoice, userId);
    }

    @Override
    public CreateOrderResponse createPublicOrder(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return createAndAttachOrder(invoice, invoice.getUserId());
    }

    private CreateOrderResponse createAndAttachOrder(Invoice invoice, String ownerUserId) {
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw new ValidationException("Only SENT invoices can create payment orders");
        }
        long amountInPaise = invoice.getTotal()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        String orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        invoiceService.attachRazorpayOrder(ownerUserId, invoice.getId(), orderId);
        return new CreateOrderResponse(orderId, razorpayConfig.getKeyId(), amountInPaise, "INR");
    }

    @Override
    public void handleWebhook(String payloadJson, String signature) {
        if (!verifySignature(payloadJson, signature, razorpayConfig.getWebhookSecret())) {
            throw new PaymentVerificationException("Invalid Razorpay signature");
        }

        RazorpayWebhookPayload payload;
        try {
            payload = objectMapper.readValue(payloadJson, RazorpayWebhookPayload.class);
        } catch (Exception ex) {
            throw new PaymentVerificationException("Invalid webhook payload");
        }

        if (payload.payload() == null || payload.payload().payment() == null) {
            throw new PaymentVerificationException("Missing payment details in webhook");
        }

        RazorpayWebhookPayload.PaymentEntity payment = payload.payload().payment();
        Invoice invoice = invoiceRepository.findByRazorpayOrderId(payment.order_id())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order id"));

        if ("captured".equalsIgnoreCase(payment.status())) {
            invoiceService.markAsPaidViaRazorpay(invoice.getId(), payment.id());
        }
    }

    private boolean verifySignature(String payload, String signature, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generated = HexFormat.of().formatHex(digest);
            return generated.equals(signature);
        } catch (Exception ex) {
            throw new PaymentVerificationException("Failed to verify webhook signature");
        }
    }
}

package com.invoicemanager.server.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.model.UserSettings;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@invoicemanager.local}")
    private String from;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void sendInvoice(User user, Invoice invoice, UserSettings settings, String recipient) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Invoice " + invoice.getId() + " from " + invoice.getDisplayName());
        message.setText(buildInvoiceEmailBody(user, invoice));
        mailSender.send(message);
    }

    private String buildInvoiceEmailBody(User user, Invoice invoice) {
        String customerName = extractValue(invoice, "customerName");
        String payLink = frontendUrl.replaceAll("/+$", "") + "/pay/" + invoice.getId();
        return String.format("""
                Dear %s,

                Please find your invoice details below:
                Invoice ID: %s
                Issued By: %s
                Total Amount: %s
                Status: %s

                You can review and pay your invoice here:
                %s

                If you have questions, please reply to this email.

                Regards,
                %s
                """,
                customerName,
                invoice.getId(),
                invoice.getDisplayName(),
                invoice.getTotal(),
                invoice.getStatus(),
                payLink,
                user.getDefaultDisplayName());
    }

    private String extractValue(Invoice invoice, String key) {
        if (invoice.getData() == null || invoice.getData().get(key) == null) {
            return "Customer";
        }
        String value = String.valueOf(invoice.getData().get(key)).trim();
        return value.isEmpty() ? "Customer" : value;
    }
}

package com.invoicemanager.server.service.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.model.UserSettings;
import com.invoicemanager.server.util.EncryptionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomSmtpEmailService implements EmailService {

    private final EncryptionUtil encryptionUtil;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void sendInvoice(User user, Invoice invoice, UserSettings settings, String recipient) {
        if (settings == null || settings.getSmtp() == null || !settings.getSmtp().isEnabled()) {
            throw new IllegalStateException("Custom SMTP settings are not enabled");
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(settings.getSmtp().getHost());
        sender.setPort(settings.getSmtp().getPort());
        sender.setUsername(settings.getSmtp().getEmail());
        sender.setPassword(encryptionUtil.decrypt(settings.getSmtp().getEncryptedAppPassword()));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(settings.getSmtp().getEmail());
        message.setTo(recipient);
        message.setSubject("Invoice " + invoice.getId() + " from " + invoice.getDisplayName());
        message.setText(buildInvoiceEmailBody(user, invoice));
        sender.send(message);
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

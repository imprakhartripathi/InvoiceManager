package com.invoicemanager.server.service.email;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.model.UserSettings;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(DefaultEmailService.class);
    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${spring.mail.username:no-reply@invoicemanager.local}")
    private String from;

    @Value("${app.sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.from-email:no-reply@invoicemanager.local}")
    private String sendGridFromEmail;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void sendInvoice(User user, Invoice invoice, UserSettings settings, String recipient) {
        String subject = "Invoice " + invoice.getId() + " from " + invoice.getDisplayName();
        String body = buildInvoiceEmailBody(user, invoice);

        if (StringUtils.hasText(sendGridApiKey)) {
            sendViaSendGrid(recipient, subject, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Default SMTP send failed for invoiceId={} recipient={} from={}",
                    invoice.getId(),
                    recipient,
                    from,
                    ex);
            throw ex;
        }
    }

    private void sendViaSendGrid(String recipient, String subject, String body) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "personalizations", new Object[] { Map.of("to", new Object[] { Map.of("email", recipient) }) },
                    "from", Map.of("email", sendGridFromEmail),
                    "subject", subject,
                    "content", new Object[] { Map.of("type", "text/plain", "value", body) }));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to construct SendGrid request payload", ex);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SENDGRID_API_URL))
                .header("Authorization", "Bearer " + sendGridApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "SendGrid API request failed with status " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception ex) {
            log.error("SendGrid send failed for recipient={} from={}", recipient, sendGridFromEmail, ex);
            throw new IllegalStateException("SendGrid delivery failed", ex);
        }
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

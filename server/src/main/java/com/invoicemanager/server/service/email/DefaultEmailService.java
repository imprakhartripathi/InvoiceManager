package com.invoicemanager.server.service.email;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
        String subject = InvoiceEmailTemplateUtil.buildSubject(invoice);
        String textBody = InvoiceEmailTemplateUtil.buildTextBody(user, invoice, frontendUrl);
        String htmlBody = InvoiceEmailTemplateUtil.buildHtmlBody(user, invoice, frontendUrl);

        if (StringUtils.hasText(sendGridApiKey)) {
            sendViaSendGrid(recipient, subject, textBody, htmlBody);
            return;
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Default SMTP send failed for invoiceId={} recipient={} from={}",
                    invoice.getId(),
                    recipient,
                    from,
                    ex);
            throw new IllegalStateException("Default SMTP delivery failed", ex);
        }
    }

    private void sendViaSendGrid(String recipient, String subject, String textBody, String htmlBody) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "personalizations", new Object[] { Map.of("to", new Object[] { Map.of("email", recipient) }) },
                    "from", Map.of("email", sendGridFromEmail),
                    "subject", subject,
                    "content", new Object[] {
                            Map.of("type", "text/plain", "value", textBody),
                            Map.of("type", "text/html", "value", htmlBody)
                    }));
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
}

package com.invoicemanager.server.service.email;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.model.UserSettings;
import com.invoicemanager.server.util.EncryptionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomSmtpEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(CustomSmtpEmailService.class);

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
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        String subject = InvoiceEmailTemplateUtil.buildSubject(invoice);
        String textBody = InvoiceEmailTemplateUtil.buildTextBody(user, invoice, frontendUrl);
        String htmlBody = InvoiceEmailTemplateUtil.buildHtmlBody(user, invoice, frontendUrl);
        try {
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(settings.getSmtp().getEmail());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);
            sender.send(message);
        } catch (Exception ex) {
            log.error("Custom SMTP send failed for invoiceId={} recipient={} smtpHost={} smtpEmail={}",
                    invoice.getId(),
                    recipient,
                    settings.getSmtp().getHost(),
                    settings.getSmtp().getEmail(),
                    ex);
            throw new IllegalStateException("Custom SMTP delivery failed", ex);
        }
    }
}

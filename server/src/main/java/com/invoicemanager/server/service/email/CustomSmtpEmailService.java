package com.invoicemanager.server.service.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
        message.setSubject("Invoice " + invoice.getId());
        message.setText("Your invoice total is " + invoice.getTotal());
        sender.send(message);
    }
}

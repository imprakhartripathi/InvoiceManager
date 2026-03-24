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

    @Override
    public void sendInvoice(User user, Invoice invoice, UserSettings settings, String recipient) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Invoice " + invoice.getId());
        message.setText("Your invoice total is " + invoice.getTotal());
        mailSender.send(message);
    }
}

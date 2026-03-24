package com.invoicemanager.server.service.email;

import org.springframework.stereotype.Service;

import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.model.UserSettings;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailOrchestratorService {

    private final DefaultEmailService defaultEmailService;
    private final CustomSmtpEmailService customSmtpEmailService;

    public void sendInvoice(User user, Invoice invoice, UserSettings settings, String recipient) {
        if (settings != null && settings.getSmtp() != null && settings.getSmtp().isEnabled()) {
            customSmtpEmailService.sendInvoice(user, invoice, settings, recipient);
            return;
        }
        defaultEmailService.sendInvoice(user, invoice, settings, recipient);
    }
}

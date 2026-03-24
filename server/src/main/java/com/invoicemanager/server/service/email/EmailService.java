package com.invoicemanager.server.service.email;

import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.model.UserSettings;

public interface EmailService {
    void sendInvoice(User user, Invoice invoice, UserSettings settings, String recipient);
}

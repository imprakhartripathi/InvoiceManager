package com.invoicemanager.server.service.email;

import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.User;

public final class InvoiceEmailTemplateUtil {

    private InvoiceEmailTemplateUtil() {
    }

    public static String buildSubject(Invoice invoice) {
        return "Invoice " + invoice.getId() + " from " + invoice.getDisplayName();
    }

    public static String buildHtmlBody(User user, Invoice invoice, String frontendUrl) {
        String customerName = extractValue(invoice, "customerName");
        String payLink = normalizeFrontendUrl(frontendUrl) + "/pay/" + invoice.getId();
        String issuedBy = safe(invoice.getDisplayName());
        String invoiceId = safe(invoice.getId());
        String total = safe(String.valueOf(invoice.getTotal()));
        String status = safe(String.valueOf(invoice.getStatus()));
        String signedBy = safe(user.getDefaultDisplayName());

        return """
                <html>
                  <body style="margin:0;padding:0;background:#f1f5f9;font-family:Arial,Helvetica,sans-serif;color:#0f172a;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="padding:24px 12px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:640px;background:#ffffff;border:1px solid #e2e8f0;border-radius:14px;overflow:hidden;">
                            <tr>
                              <td style="padding:20px 24px;background:linear-gradient(135deg,#0f172a,#0e7490);color:#ffffff;">
                                <p style="margin:0;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;opacity:0.9;">Invoice Notice</p>
                                <h2 style="margin:8px 0 0;font-size:24px;line-height:1.2;">Payment Request</h2>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:24px;">
                                <p style="margin:0 0 16px;font-size:15px;">Hi %s,</p>
                                <p style="margin:0 0 18px;font-size:14px;color:#334155;">
                                  A new invoice has been issued for your review.
                                </p>
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;">
                                  <tr>
                                    <td style="padding:12px 14px;font-size:13px;color:#64748b;">Invoice ID</td>
                                    <td style="padding:12px 14px;font-size:13px;font-weight:600;text-align:right;">%s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:12px 14px;font-size:13px;color:#64748b;">Issued By</td>
                                    <td style="padding:12px 14px;font-size:13px;font-weight:600;text-align:right;">%s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:12px 14px;font-size:13px;color:#64748b;">Total Amount</td>
                                    <td style="padding:12px 14px;font-size:13px;font-weight:600;text-align:right;">%s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:12px 14px;font-size:13px;color:#64748b;">Status</td>
                                    <td style="padding:12px 14px;font-size:13px;font-weight:600;text-align:right;">%s</td>
                                  </tr>
                                </table>
                                <p style="margin:22px 0 12px;font-size:14px;color:#334155;">Click below to view and pay:</p>
                                <p style="margin:0 0 22px;">
                                  <a href="%s" style="display:inline-block;background:#0b5fc0;color:#ffffff;text-decoration:none;padding:11px 18px;border-radius:9px;font-weight:600;font-size:14px;">
                                    View & Pay Invoice
                                  </a>
                                </p>
                                <p style="margin:0;font-size:13px;color:#64748b;">
                                  If the button does not work, copy this link:<br />
                                  <a href="%s" style="color:#0b5fc0;text-decoration:none;">%s</a>
                                </p>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:16px 24px;background:#f8fafc;border-top:1px solid #e2e8f0;color:#64748b;font-size:12px;">
                                Regards,<br />%s
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """
                .formatted(
                        safe(customerName),
                        invoiceId,
                        issuedBy,
                        total,
                        status,
                        payLink,
                        payLink,
                        payLink,
                        signedBy);
    }

    public static String buildTextBody(User user, Invoice invoice, String frontendUrl) {
        String customerName = extractValue(invoice, "customerName");
        String payLink = normalizeFrontendUrl(frontendUrl) + "/pay/" + invoice.getId();
        return """
                Dear %s,

                Please find your invoice details below:
                Invoice ID: %s
                Issued By: %s
                Total Amount: %s
                Status: %s

                View and pay your invoice:
                %s

                Regards,
                %s
                """
                .formatted(
                        customerName,
                        invoice.getId(),
                        invoice.getDisplayName(),
                        invoice.getTotal(),
                        invoice.getStatus(),
                        payLink,
                        user.getDefaultDisplayName());
    }

    private static String extractValue(Invoice invoice, String key) {
        if (invoice.getData() == null || invoice.getData().get(key) == null) {
            return "Customer";
        }
        String value = String.valueOf(invoice.getData().get(key)).trim();
        return value.isEmpty() ? "Customer" : value;
    }

    private static String normalizeFrontendUrl(String frontendUrl) {
        String value = frontendUrl == null ? "" : frontendUrl.trim();
        if (value.isEmpty()) {
            return "http://localhost:4200";
        }
        return value.replaceAll("/+$", "");
    }

    private static String safe(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

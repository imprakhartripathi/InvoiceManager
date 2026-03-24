package com.invoicemanager.server.service.invoice;

import java.util.List;

import com.invoicemanager.server.dto.invoice.CreateInvoiceRequest;
import com.invoicemanager.server.dto.invoice.InvoiceResponse;
import com.invoicemanager.server.dto.invoice.InvoiceSummaryResponse;

public interface InvoiceService {
    InvoiceResponse createInvoice(String userId, CreateInvoiceRequest request);

    List<InvoiceResponse> getMyInvoices(String userId);

    InvoiceResponse getInvoice(String userId, String invoiceId);

    InvoiceSummaryResponse getPublicInvoiceSummary(String invoiceId);

    void markAsPaid(String invoiceId, String razorpayPaymentId);

    InvoiceResponse attachRazorpayOrder(String userId, String invoiceId, String razorpayOrderId);
}

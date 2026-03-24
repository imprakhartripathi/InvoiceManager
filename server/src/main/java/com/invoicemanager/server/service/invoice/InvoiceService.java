package com.invoicemanager.server.service.invoice;

import java.util.List;

import com.invoicemanager.server.dto.invoice.CreateInvoiceRequest;
import com.invoicemanager.server.dto.invoice.InvoiceResponse;
import com.invoicemanager.server.dto.invoice.InvoiceSummaryResponse;
import com.invoicemanager.server.dto.invoice.UpdateInvoiceRequest;

public interface InvoiceService {
    InvoiceResponse createInvoice(String userId, CreateInvoiceRequest request);

    InvoiceResponse updateInvoice(String userId, String invoiceId, UpdateInvoiceRequest request);

    void deleteInvoice(String userId, String invoiceId);

    List<InvoiceResponse> getMyInvoices(String userId);

    InvoiceResponse getInvoice(String userId, String invoiceId);

    InvoiceSummaryResponse getPublicInvoiceSummary(String invoiceId);

    InvoiceResponse markAsSent(String userId, String invoiceId);

    InvoiceResponse markAsPaidCash(String userId, String invoiceId);

    void markAsPaidViaRazorpay(String invoiceId, String razorpayPaymentId);

    InvoiceResponse attachRazorpayOrder(String userId, String invoiceId, String razorpayOrderId);
}

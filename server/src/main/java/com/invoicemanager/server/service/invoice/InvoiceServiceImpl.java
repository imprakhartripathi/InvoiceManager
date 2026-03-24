package com.invoicemanager.server.service.invoice;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.invoicemanager.server.dto.invoice.CreateInvoiceRequest;
import com.invoicemanager.server.dto.invoice.InvoiceResponse;
import com.invoicemanager.server.dto.invoice.InvoiceSummaryResponse;
import com.invoicemanager.server.exception.ResourceNotFoundException;
import com.invoicemanager.server.exception.ValidationException;
import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.InvoiceStatus;
import com.invoicemanager.server.model.Template;
import com.invoicemanager.server.model.TemplateField;
import com.invoicemanager.server.repository.InvoiceRepository;
import com.invoicemanager.server.repository.TemplateRepository;
import com.invoicemanager.server.util.InvoiceCalculationUtil;
import com.invoicemanager.server.util.OwnershipValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final TemplateRepository templateRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceCalculationUtil invoiceCalculationUtil;
    private final OwnershipValidator ownershipValidator;

    @Override
    public InvoiceResponse createInvoice(String userId, CreateInvoiceRequest request) {
        Template template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        ownershipValidator.ensureOwner(userId, template.getUserId());
        validateTemplateData(template, request.data());

        Invoice invoice = Invoice.builder()
                .userId(userId)
                .templateId(template.getId())
                .data(request.data())
                .lineItems(template.isHasLineItems() ? request.lineItems() : List.of())
                .total(invoiceCalculationUtil.calculateTotal(request.lineItems()))
                .status(InvoiceStatus.DRAFT)
                .build();

        return toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public List<InvoiceResponse> getMyInvoices(String userId) {
        return invoiceRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public InvoiceResponse getInvoice(String userId, String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        ownershipValidator.ensureOwner(userId, invoice.getUserId());
        return toResponse(invoice);
    }

    @Override
    public InvoiceSummaryResponse getPublicInvoiceSummary(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        Template template = templateRepository.findById(invoice.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        return new InvoiceSummaryResponse(
                invoice.getId(),
                template.getName(),
                invoice.getTotal(),
                invoice.getStatus().name(),
                invoice.getRazorpayOrderId());
    }

    @Override
    public void markAsPaid(String invoiceId, String razorpayPaymentId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());
        if (invoice.getData() != null) {
            invoice.getData().put("razorpayPaymentId", razorpayPaymentId);
        }
        invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceResponse attachRazorpayOrder(String userId, String invoiceId, String razorpayOrderId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        ownershipValidator.ensureOwner(userId, invoice.getUserId());
        invoice.setRazorpayOrderId(razorpayOrderId);
        return toResponse(invoiceRepository.save(invoice));
    }

    private void validateTemplateData(Template template, Map<String, Object> data) {
        for (TemplateField field : template.getFields()) {
            Object value = data.get(field.getKey());
            if (field.isRequired() && Objects.isNull(value)) {
                throw new ValidationException("Missing required field: " + field.getKey());
            }
        }
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getUserId(),
                invoice.getTemplateId(),
                invoice.getData(),
                invoice.getLineItems(),
                invoice.getTotal(),
                invoice.getStatus().name(),
                invoice.getRazorpayOrderId(),
                invoice.getCreatedAt(),
                invoice.getPaidAt());
    }
}

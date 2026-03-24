package com.invoicemanager.server.service.invoice;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.invoicemanager.server.dto.invoice.CreateInvoiceRequest;
import com.invoicemanager.server.dto.invoice.InvoiceResponse;
import com.invoicemanager.server.dto.invoice.InvoiceSummaryResponse;
import com.invoicemanager.server.dto.invoice.UpdateInvoiceRequest;
import com.invoicemanager.server.exception.ResourceNotFoundException;
import com.invoicemanager.server.exception.ValidationException;
import com.invoicemanager.server.model.Invoice;
import com.invoicemanager.server.model.PaidBy;
import com.invoicemanager.server.model.PaidVia;
import com.invoicemanager.server.model.InvoiceStatus;
import com.invoicemanager.server.model.Template;
import com.invoicemanager.server.model.TemplateField;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.model.UserSettings;
import com.invoicemanager.server.repository.InvoiceRepository;
import com.invoicemanager.server.repository.TemplateRepository;
import com.invoicemanager.server.repository.UserRepository;
import com.invoicemanager.server.repository.UserSettingsRepository;
import com.invoicemanager.server.service.email.EmailOrchestratorService;
import com.invoicemanager.server.util.InvoiceCalculationUtil;
import com.invoicemanager.server.util.OwnershipValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceServiceImpl.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final TemplateRepository templateRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final EmailOrchestratorService emailOrchestratorService;
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
                .displayName(request.displayName().trim())
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
    public InvoiceResponse updateInvoice(String userId, String invoiceId, UpdateInvoiceRequest request) {
        throw new ValidationException("Invoice editing is not allowed");
    }

    @Override
    public void deleteInvoice(String userId, String invoiceId) {
        throw new ValidationException("Invoice deletion is not allowed");
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
    public InvoiceResponse markAsSent(String userId, String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        ownershipValidator.ensureOwner(userId, invoice.getUserId());

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ValidationException("Only DRAFT invoices can be marked as SENT");
        }

        String recipient = extractStringField(invoice.getData(), "customerEmail");
        if (!EMAIL_PATTERN.matcher(recipient).matches()) {
            throw new ValidationException("Invoice requires a valid customerEmail before sending");
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserSettings settings = userSettingsRepository.findByUserId(userId).orElse(null);

        invoice.setStatus(InvoiceStatus.SENT);
        try {
            emailOrchestratorService.sendInvoice(owner, invoice, settings, recipient);
        } catch (Exception ex) {
            log.error("Invoice send email failed userId={} invoiceId={} recipient={}",
                    userId, invoiceId, recipient, ex);
            throw new ValidationException("Unable to send invoice email. Check SMTP settings and recipient email.");
        }
        return toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse markAsPaidCash(String userId, String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        ownershipValidator.ensureOwner(userId, invoice.getUserId());
        ensurePayable(invoice);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidVia(PaidVia.CASH);
        invoice.setPaidBy(PaidBy.OWNER);
        invoice.setPaidAt(Instant.now());
        return toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public void markAsPaidViaRazorpay(String invoiceId, String razorpayPaymentId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        ensurePayable(invoice);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidVia(PaidVia.RAZORPAY);
        invoice.setPaidBy(PaidBy.CLIENT);
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
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw new ValidationException("Only SENT invoices can create payment orders");
        }
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
                invoice.getDisplayName(),
                invoice.getData(),
                invoice.getLineItems(),
                invoice.getTotal(),
                invoice.getStatus().name(),
                invoice.getRazorpayOrderId(),
                invoice.getPaidVia() == null ? null : invoice.getPaidVia().name(),
                invoice.getPaidBy() == null ? null : invoice.getPaidBy().name(),
                invoice.getCreatedAt(),
                invoice.getPaidAt());
    }

    private void ensurePayable(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new ValidationException("DRAFT invoices cannot be paid");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new ValidationException("Invoice is already PAID");
        }
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw new ValidationException("Only SENT invoices can be marked as PAID");
        }
    }

    private String extractStringField(Map<String, Object> data, String key) {
        Object value = data == null ? null : data.get(key);
        String parsed = value == null ? "" : String.valueOf(value).trim();
        if (parsed.isEmpty()) {
            throw new ValidationException("Missing required field: " + key);
        }
        return parsed;
    }
}

package com.invoicemanager.server.service.template;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.invoicemanager.server.dto.template.TemplateFieldDto;
import com.invoicemanager.server.dto.template.TemplateRequest;
import com.invoicemanager.server.dto.template.TemplateResponse;
import com.invoicemanager.server.exception.ResourceNotFoundException;
import com.invoicemanager.server.exception.ValidationException;
import com.invoicemanager.server.model.Template;
import com.invoicemanager.server.model.TemplateField;
import com.invoicemanager.server.repository.InvoiceRepository;
import com.invoicemanager.server.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private static final Set<String> ALLOWED_TYPES = Set.of("text", "number", "date");

    private final TemplateRepository templateRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public TemplateResponse create(String userId, TemplateRequest request) {
        validateFields(request.fields());

        Template template = Template.builder()
                .userId(userId)
                .name(request.name())
                .fields(request.fields().stream().map(this::fromDto).toList())
                .hasLineItems(request.hasLineItems())
                .build();

        return toResponse(templateRepository.save(template));
    }

    @Override
    public TemplateResponse update(String userId, String templateId, TemplateRequest request) {
        Template template = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        validateFields(request.fields());
        template.setName(request.name());
        template.setFields(request.fields().stream().map(this::fromDto).toList());
        template.setHasLineItems(request.hasLineItems());
        return toResponse(templateRepository.save(template));
    }

    @Override
    public void delete(String userId, String templateId) {
        Template template = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        if (invoiceRepository.existsByUserIdAndTemplateId(userId, templateId)) {
            throw new ValidationException("Template is in use by invoices and cannot be deleted");
        }

        templateRepository.delete(template);
    }

    @Override
    public List<TemplateResponse> getMyTemplates(String userId) {
        return templateRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public TemplateResponse getOne(String userId, String templateId) {
        Template template = templateRepository.findByIdAndUserId(templateId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        return toResponse(template);
    }

    private void validateFields(List<TemplateFieldDto> fields) {
        if (fields.isEmpty()) {
            throw new ValidationException("Template requires at least one field");
        }

        Set<String> keys = new HashSet<>();
        boolean hasRequiredCustomerName = false;
        boolean hasRequiredCustomerEmail = false;
        for (TemplateFieldDto field : fields) {
            String normalizedType = field.type().toLowerCase(Locale.ROOT);
            String normalizedKey = field.key().trim().toLowerCase(Locale.ROOT);
            if (!ALLOWED_TYPES.contains(normalizedType)) {
                throw new ValidationException("Unsupported field type: " + field.type());
            }
            if (!keys.add(field.key())) {
                throw new ValidationException("Duplicate field key: " + field.key());
            }

            if ("customername".equals(normalizedKey) && field.required()) {
                hasRequiredCustomerName = true;
            }
            if ("customeremail".equals(normalizedKey) && field.required()) {
                hasRequiredCustomerEmail = true;
            }
        }

        if (!hasRequiredCustomerName) {
            throw new ValidationException("Template must include required field key: customerName");
        }
        if (!hasRequiredCustomerEmail) {
            throw new ValidationException("Template must include required field key: customerEmail");
        }
    }

    private TemplateField fromDto(TemplateFieldDto field) {
        return TemplateField.builder()
                .key(field.key())
                .label(field.label())
                .type(field.type().toLowerCase(Locale.ROOT))
                .required(field.required())
                .defaultValue(field.defaultValue())
                .build();
    }

    private TemplateResponse toResponse(Template template) {
        boolean inUse = invoiceRepository.existsByUserIdAndTemplateId(template.getUserId(), template.getId());
        return new TemplateResponse(
                template.getId(),
                template.getUserId(),
                template.getName(),
                template.getFields().stream().map(this::toDto).toList(),
                template.isHasLineItems(),
                inUse);
    }

    private TemplateFieldDto toDto(TemplateField field) {
        return new TemplateFieldDto(field.getKey(), field.getLabel(), field.getType(), field.isRequired(), field.getDefaultValue());
    }
}

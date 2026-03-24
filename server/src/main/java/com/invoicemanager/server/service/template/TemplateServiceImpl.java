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
import com.invoicemanager.server.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private static final Set<String> ALLOWED_TYPES = Set.of("text", "number", "date");

    private final TemplateRepository templateRepository;

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
        for (TemplateFieldDto field : fields) {
            String normalizedType = field.type().toLowerCase(Locale.ROOT);
            if (!ALLOWED_TYPES.contains(normalizedType)) {
                throw new ValidationException("Unsupported field type: " + field.type());
            }
            if (!keys.add(field.key())) {
                throw new ValidationException("Duplicate field key: " + field.key());
            }
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
        return new TemplateResponse(
                template.getId(),
                template.getUserId(),
                template.getName(),
                template.getFields().stream().map(this::toDto).toList(),
                template.isHasLineItems());
    }

    private TemplateFieldDto toDto(TemplateField field) {
        return new TemplateFieldDto(field.getKey(), field.getLabel(), field.getType(), field.isRequired(), field.getDefaultValue());
    }
}

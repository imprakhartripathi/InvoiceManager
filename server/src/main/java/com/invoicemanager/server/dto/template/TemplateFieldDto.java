package com.invoicemanager.server.dto.template;

import jakarta.validation.constraints.NotBlank;

public record TemplateFieldDto(
        @NotBlank String key,
        @NotBlank String label,
        @NotBlank String type,
        boolean required,
        Object defaultValue) {
}

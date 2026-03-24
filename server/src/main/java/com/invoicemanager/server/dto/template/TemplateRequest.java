package com.invoicemanager.server.dto.template;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TemplateRequest(
        @NotBlank String name,
        @NotNull List<@Valid TemplateFieldDto> fields,
        boolean hasLineItems) {
}

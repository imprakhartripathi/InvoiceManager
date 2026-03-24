package com.invoicemanager.server.dto.template;

import java.util.List;

public record TemplateResponse(
        String id,
        String userId,
        String name,
        List<TemplateFieldDto> fields,
        boolean hasLineItems) {
}

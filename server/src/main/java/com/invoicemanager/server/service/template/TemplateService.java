package com.invoicemanager.server.service.template;

import java.util.List;

import com.invoicemanager.server.dto.template.TemplateRequest;
import com.invoicemanager.server.dto.template.TemplateResponse;

public interface TemplateService {
    TemplateResponse create(String userId, TemplateRequest request);

    List<TemplateResponse> getMyTemplates(String userId);

    TemplateResponse getOne(String userId, String templateId);
}

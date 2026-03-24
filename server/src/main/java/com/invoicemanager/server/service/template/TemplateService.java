package com.invoicemanager.server.service.template;

import java.util.List;

import com.invoicemanager.server.dto.template.TemplateRequest;
import com.invoicemanager.server.dto.template.TemplateResponse;

public interface TemplateService {
    TemplateResponse create(String userId, TemplateRequest request);

    TemplateResponse update(String userId, String templateId, TemplateRequest request);

    void delete(String userId, String templateId);

    List<TemplateResponse> getMyTemplates(String userId);

    TemplateResponse getOne(String userId, String templateId);
}

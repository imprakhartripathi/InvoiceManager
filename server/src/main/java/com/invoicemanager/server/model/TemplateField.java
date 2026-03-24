package com.invoicemanager.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateField {
    private String key;
    private String label;
    private String type;
    private boolean required;
    private Object defaultValue;
}

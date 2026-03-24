package com.invoicemanager.server.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.invoicemanager.server.model.audit.BaseDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "templates")
public class Template extends BaseDocument {
    private String userId;
    private String name;
    private List<TemplateField> fields;
    private boolean hasLineItems;
}

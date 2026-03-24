package com.invoicemanager.server.model;

import java.util.List;

import org.springframework.data.mongodb.core.index.Indexed;
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
@Document(collection = "users")
public class User extends BaseDocument {
    @Indexed(unique = true)
    private String email;

    private String password;
    private String fullName;
    private String businessName;
    private String defaultDisplayName;
    private List<String> savedCustomDisplayNames;
}

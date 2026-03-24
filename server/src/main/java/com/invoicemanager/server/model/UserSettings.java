package com.invoicemanager.server.model;

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
@Document(collection = "user_settings")
public class UserSettings extends BaseDocument {
    @Indexed(unique = true)
    private String userId;

    private SmtpConfig smtp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmtpConfig {
        private String host;
        private Integer port;
        private String email;
        private String encryptedAppPassword;
        private boolean enabled;
    }
}

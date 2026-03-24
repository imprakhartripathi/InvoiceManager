package com.invoicemanager.server.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    @Value("${app.crypto.secret:change-me}")
    private String secret;

    public String encrypt(String plainText) {
        String value = plainText + ":" + secret;
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public String decrypt(String encrypted) {
        String decoded = new String(Base64.getDecoder().decode(encrypted), StandardCharsets.UTF_8);
        String suffix = ":" + secret;
        if (!decoded.endsWith(suffix)) {
            throw new IllegalArgumentException("Cannot decrypt value with configured secret");
        }
        return decoded.substring(0, decoded.length() - suffix.length());
    }
}

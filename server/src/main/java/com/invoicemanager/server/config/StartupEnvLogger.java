package com.invoicemanager.server.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupEnvLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupEnvLogger.class);
    private final Environment environment;

    private static final List<String> KEYS = List.of(
            "MONGODB_URI",
            "spring.mongodb.uri",
            "spring.data.mongodb.uri",
            "JWT_SECRET",
            "app.jwt.secret",
            "APP_CRYPTO_SECRET",
            "app.crypto.secret",
            "MAIL_USERNAME",
            "spring.mail.username",
            "MAIL_PASSWORD",
            "spring.mail.password",
            "SENDGRID_API_KEY",
            "app.sendgrid.api-key",
            "SENDGRID_FROM_EMAIL",
            "app.sendgrid.from-email",
            "RAZORPAY_KEY_ID",
            "app.razorpay.key-id",
            "RAZORPAY_KEY_SECRET",
            "app.razorpay.key-secret",
            "RAZORPAY_WEBHOOK_SECRET");

    public StartupEnvLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Startup configuration check (sensitive values masked):");
        for (String key : KEYS) {
            // Reads from Spring property sources: system properties (-D), env vars, and config placeholders.
            String value = environment.getProperty(key);
            if (value == null || value.isBlank()) {
                log.warn("{} = <not set>", key);
            } else {
                log.info("{} = {}", key, mask(value));
            }
        }
    }

    private String mask(String value) {
        int len = value.length();
        if (len <= 4) {
            return "****";
        }
        int keepStart = Math.min(3, len / 2);
        int keepEnd = Math.min(2, len - keepStart);
        String start = value.substring(0, keepStart);
        String end = value.substring(len - keepEnd);
        return start + "*".repeat(Math.max(4, len - keepStart - keepEnd)) + end;
    }
}

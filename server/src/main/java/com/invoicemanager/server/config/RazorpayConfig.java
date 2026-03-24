package com.invoicemanager.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.razorpay")
public class RazorpayConfig {
    private String keyId;
    private String keySecret;
    private String webhookSecret;
}

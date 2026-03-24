package com.invoicemanager.server.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

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
@Document(collection = "invoices")
public class Invoice extends BaseDocument {
    private String userId;
    private String templateId;
    private Map<String, Object> data;
    private List<Map<String, Object>> lineItems;
    private BigDecimal total;
    private InvoiceStatus status;
    private String razorpayOrderId;
    private Instant paidAt;
}

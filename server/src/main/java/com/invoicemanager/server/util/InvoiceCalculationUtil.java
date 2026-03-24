package com.invoicemanager.server.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class InvoiceCalculationUtil {

    public BigDecimal calculateTotal(List<Map<String, Object>> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return lineItems.stream()
                .map(this::calculateLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateLineTotal(Map<String, Object> item) {
        BigDecimal quantity = toDecimal(item.getOrDefault("quantity", 1));
        BigDecimal unitPrice = toDecimal(item.getOrDefault("unitPrice", item.getOrDefault("amount", 0)));
        return quantity.multiply(unitPrice);
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}

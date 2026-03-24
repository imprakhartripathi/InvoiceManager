package com.invoicemanager.server.util;

import org.springframework.stereotype.Component;

import com.invoicemanager.server.exception.ForbiddenException;

@Component
public class OwnershipValidator {
    public void ensureOwner(String expectedUserId, String actualUserId) {
        if (!expectedUserId.equals(actualUserId)) {
            throw new ForbiddenException("Resource does not belong to authenticated user");
        }
    }
}

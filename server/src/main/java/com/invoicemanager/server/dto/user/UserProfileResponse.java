package com.invoicemanager.server.dto.user;

import java.util.List;

public record UserProfileResponse(
        String fullName,
        String businessName,
        String defaultDisplayName,
        List<String> savedCustomDisplayNames) {
}

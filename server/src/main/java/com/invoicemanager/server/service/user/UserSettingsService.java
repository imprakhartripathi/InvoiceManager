package com.invoicemanager.server.service.user;

import com.invoicemanager.server.dto.user.UserSettingsRequest;
import com.invoicemanager.server.dto.user.UserSettingsResponse;

public interface UserSettingsService {
    UserSettingsResponse upsertSettings(String userId, UserSettingsRequest request);

    UserSettingsResponse getSettings(String userId);
}

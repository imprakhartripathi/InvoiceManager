package com.invoicemanager.server.service.user;

import com.invoicemanager.server.dto.user.UserProfileResponse;
import com.invoicemanager.server.dto.user.UserProfileUpdateRequest;

public interface UserProfileService {
    UserProfileResponse getProfile(String userId);

    UserProfileResponse updateProfile(String userId, UserProfileUpdateRequest request);
}

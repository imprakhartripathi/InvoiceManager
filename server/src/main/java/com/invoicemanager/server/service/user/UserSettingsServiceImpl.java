package com.invoicemanager.server.service.user;

import org.springframework.stereotype.Service;

import com.invoicemanager.server.dto.user.UserSettingsRequest;
import com.invoicemanager.server.dto.user.UserSettingsResponse;
import com.invoicemanager.server.model.UserSettings;
import com.invoicemanager.server.repository.UserSettingsRepository;
import com.invoicemanager.server.util.EncryptionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final EncryptionUtil encryptionUtil;

    @Override
    public UserSettingsResponse upsertSettings(String userId, UserSettingsRequest request) {
        UserSettings current = userSettingsRepository.findByUserId(userId)
                .orElse(UserSettings.builder().userId(userId).build());

        current.setSmtp(UserSettings.SmtpConfig.builder()
                .host(request.host())
                .port(request.port())
                .email(request.email())
                .encryptedAppPassword(encryptionUtil.encrypt(request.appPassword()))
                .enabled(request.enabled())
                .build());

        UserSettings saved = userSettingsRepository.save(current);
        return toResponse(saved);
    }

    @Override
    public UserSettingsResponse getSettings(String userId) {
        return userSettingsRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElse(new UserSettingsResponse(null, null, null, false));
    }

    private UserSettingsResponse toResponse(UserSettings settings) {
        UserSettings.SmtpConfig smtp = settings.getSmtp();
        if (smtp == null) {
            return new UserSettingsResponse(null, null, null, false);
        }
        return new UserSettingsResponse(smtp.getHost(), smtp.getPort(), smtp.getEmail(), smtp.isEnabled());
    }
}

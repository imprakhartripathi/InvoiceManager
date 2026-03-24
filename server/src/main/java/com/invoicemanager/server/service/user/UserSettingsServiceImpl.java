package com.invoicemanager.server.service.user;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.invoicemanager.server.dto.user.UserSettingsRequest;
import com.invoicemanager.server.dto.user.UserSettingsResponse;
import com.invoicemanager.server.exception.ValidationException;
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
        UserSettings.SmtpConfig existing = current.getSmtp();

        if (request.enabled()) {
            if (!StringUtils.hasText(request.host())) {
                throw new ValidationException("SMTP host is required when custom SMTP is enabled");
            }
            if (request.port() == null) {
                throw new ValidationException("SMTP port is required when custom SMTP is enabled");
            }
            if (!StringUtils.hasText(request.email())) {
                throw new ValidationException("SMTP sender email is required when custom SMTP is enabled");
            }
            boolean hasExistingPassword = existing != null && StringUtils.hasText(existing.getEncryptedAppPassword());
            if (!StringUtils.hasText(request.appPassword()) && !hasExistingPassword) {
                throw new ValidationException("SMTP app password is required when enabling custom SMTP");
            }
        }

        String encryptedPassword = existing == null ? null : existing.getEncryptedAppPassword();
        if (StringUtils.hasText(request.appPassword())) {
            encryptedPassword = encryptionUtil.encrypt(request.appPassword());
        }

        current.setSmtp(UserSettings.SmtpConfig.builder()
                .host(StringUtils.hasText(request.host()) ? request.host() : (existing == null ? null : existing.getHost()))
                .port(request.port() != null ? request.port() : (existing == null ? null : existing.getPort()))
                .email(StringUtils.hasText(request.email()) ? request.email() : (existing == null ? null : existing.getEmail()))
                .encryptedAppPassword(encryptedPassword)
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

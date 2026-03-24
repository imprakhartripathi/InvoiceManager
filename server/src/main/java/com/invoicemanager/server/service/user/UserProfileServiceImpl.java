package com.invoicemanager.server.service.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.invoicemanager.server.dto.user.UserProfileResponse;
import com.invoicemanager.server.dto.user.UserProfileUpdateRequest;
import com.invoicemanager.server.exception.ResourceNotFoundException;
import com.invoicemanager.server.exception.ValidationException;
import com.invoicemanager.server.model.User;
import com.invoicemanager.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(String userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String fullName = request.fullName().trim();
        String businessName = request.businessName() == null ? "" : request.businessName().trim();
        String defaultDisplayName = request.defaultDisplayName().trim();
        String newCustomDisplayName = request.newCustomDisplayName() == null ? "" : request.newCustomDisplayName().trim();

        if (defaultDisplayName.isBlank()) {
            throw new ValidationException("defaultDisplayName is required");
        }

        Set<String> names = new LinkedHashSet<>();
        if (user.getSavedCustomDisplayNames() != null) {
            user.getSavedCustomDisplayNames().stream().map(String::trim).filter(s -> !s.isBlank()).forEach(names::add);
        }
        if (!newCustomDisplayName.isBlank()) {
            names.add(newCustomDisplayName);
        }

        user.setFullName(fullName);
        user.setBusinessName(businessName);
        user.setDefaultDisplayName(defaultDisplayName);
        user.setSavedCustomDisplayNames(new ArrayList<>(names));

        return toResponse(userRepository.save(user));
    }

    private UserProfileResponse toResponse(User user) {
        List<String> names = user.getSavedCustomDisplayNames() == null ? List.of() : user.getSavedCustomDisplayNames();
        return new UserProfileResponse(
                user.getFullName(),
                user.getBusinessName(),
                user.getDefaultDisplayName(),
                names);
    }
}

package com.taskOrchestrator.app.profile.service;

import com.taskOrchestrator.app.auth.application.CurrentUser;
import com.taskOrchestrator.app.auth.application.CurrentUserProvider;
import com.taskOrchestrator.app.auth.application.password.PasswordPolicyValidator;
import com.taskOrchestrator.app.auth.domain.User;
import com.taskOrchestrator.app.auth.domain.UserRepository;
import com.taskOrchestrator.app.profile.dto.UpdatePasswordRequest;
import com.taskOrchestrator.app.profile.storage.FileStorageService;
import com.taskOrchestrator.app.profile.dto.UpdateProfileRequest;
import com.taskOrchestrator.app.profile.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserProfileService {
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public UserProfileService(UserRepository userRepository, CurrentUserProvider currentUserProvider,
                              FileStorageService fileStorageService, PasswordEncoder passwordEncoder, PasswordPolicyValidator passwordPolicyValidator) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public UserProfileResponse getProfile() {
        CurrentUser current = currentUserProvider.getCurrentUser();
        User user = getUser(current.username());
        return UserProfileResponse.fromUser(user);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        CurrentUser current = currentUserProvider.getCurrentUser();
        User user = getUser(current.username());

        user.setEmail(request.email());
        user.setFullName(request.fullName());

        userRepository.save(user);

        return UserProfileResponse.fromUser(user);
    }

    public UserProfileResponse updatePassword(UpdatePasswordRequest request) {
        CurrentUser current = currentUserProvider.getCurrentUser();
        User user = getUser(current.username());

        passwordPolicyValidator.validate(
                user,
                request.currentPassword(),
                request.newPassword()
        );

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.saveAndFlush(user);

        return UserProfileResponse.fromUser(user);
    }

    public UserProfileResponse uploadAvatar(MultipartFile file) throws IOException {
        CurrentUser current = currentUserProvider.getCurrentUser();
        User user = getUser(current.username());

        String avatarUrl = fileStorageService.store(file);
        user.setProfilePictureUrl(avatarUrl);

        userRepository.saveAndFlush(user);

        return UserProfileResponse.fromUser(user);
    }
}

package com.taskOrchestrator.app.profile.controller;

import com.taskOrchestrator.app.profile.dto.UpdatePasswordRequest;
import com.taskOrchestrator.app.profile.dto.UpdateProfileRequest;
import com.taskOrchestrator.app.profile.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.taskOrchestrator.app.profile.service.UserProfileService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public UserProfileResponse getProfile() {
        return userProfileService.getProfile();
    }

    @PutMapping("/profile")
    public UserProfileResponse updateProfile(@RequestBody UpdateProfileRequest request) {
        return userProfileService.updateProfile(request);
    }

    @PutMapping("profile/password")
    public UserProfileResponse updatePassword(@RequestBody UpdatePasswordRequest request) {
        return userProfileService.updatePassword(request);
    }

    @PostMapping("/profile/avatar")
    public UserProfileResponse uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return userProfileService.uploadAvatar(file);
    }
}
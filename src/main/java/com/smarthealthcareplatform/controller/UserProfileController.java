package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.UserProfileRequest;
import com.smarthealthcareplatform.dto.UserProfileResponse;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.entity.UserProfile;
import com.smarthealthcareplatform.repository.UserProfileRepository;
import com.smarthealthcareplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // CORE-03: Bệnh nhân/Bác sĩ xem hồ sơ cá nhân — BUG-07 FIX: trả DTO
    @GetMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ cá nhân"));

        return ResponseEntity.ok(mapToResponse(profile));
    }

    // CORE-03: Cập nhật hồ sơ cá nhân — BUG-07 FIX: trả DTO
    @PutMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UserProfileRequest request, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ cá nhân"));

        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());

        return ResponseEntity.ok(mapToResponse(userProfileRepository.save(profile)));
    }

    // BUG-07 FIX: Mapper Entity → DTO
    private UserProfileResponse mapToResponse(UserProfile p) {
        return UserProfileResponse.builder()
                .userId(p.getUserId())
                .fullName(p.getFullName())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .phoneNumber(p.getPhoneNumber())
                .address(p.getAddress())
                .build();
    }
}

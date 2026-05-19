package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.UserResponse;
import com.smarthealthcareplatform.entity.Role;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.entity.UserProfile;
import com.smarthealthcareplatform.repository.UserProfileRepository;
import com.smarthealthcareplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý người dùng — dành cho Admin.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // Lấy danh sách tất cả người dùng
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Lấy 1 người dùng theo ID
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + id));
        return mapToResponse(user);
    }

    // Khóa / Mở khóa tài khoản (Toggle Active)
    @Transactional
    public UserResponse toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));
        user.setIsActive(!user.getIsActive());
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    // Cập nhật vai trò (PATIENT ↔ DOCTOR). Không cho phép đổi sang ADMIN.
    @Transactional
    public UserResponse updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        // Không cho đổi vai trò của chính ADMIN
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể thay đổi vai trò của tài khoản Admin!");
        }

        try {
            Role role = Role.valueOf(newRole.toUpperCase());
            if (role == Role.ADMIN) {
                throw new RuntimeException("Không được phép gán vai trò ADMIN qua chức năng này!");
            }
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ: " + newRole);
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    // Xóa người dùng (hard delete) — chỉ dùng khi thật sự cần thiết
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể xóa tài khoản Admin!");
        }

        // Xóa profile trước (FK constraint)
        userProfileRepository.findById(userId).ifPresent(userProfileRepository::delete);
        userRepository.delete(user);
    }

    // Mapper Entity → DTO
    private UserResponse mapToResponse(User user) {
        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .fullName(profile != null ? profile.getFullName() : "N/A")
                .phoneNumber(profile != null ? profile.getPhoneNumber() : "N/A")
                .gender(profile != null ? profile.getGender() : "N/A")
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .address(profile != null ? profile.getAddress() : "N/A")
                .build();
    }
}

package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.UserResponse;
import com.smarthealthcareplatform.entity.Role;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.entity.UserProfile;
import com.smarthealthcareplatform.entity.Doctor;
import com.smarthealthcareplatform.entity.Specialty;
import com.smarthealthcareplatform.repository.UserProfileRepository;
import com.smarthealthcareplatform.repository.UserRepository;
import com.smarthealthcareplatform.repository.DoctorRepository;
import com.smarthealthcareplatform.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

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

        // Defensive Programming: Nếu mở khóa cho DOCTOR mà chưa có bản ghi trong doctors, tự động tạo bản ghi mặc định
        if (user.getIsActive() && user.getRole() == Role.DOCTOR) {
            ensureDoctorRecordExists(user);
        }

        return mapToResponse(user);
    }

    // Phê duyệt tài khoản Bác sĩ với thông số lâm sàng chuyên nghiệp
    @Transactional
    public UserResponse approveDoctor(Long userId, Long specialtyId, Integer experienceYears, String qualifications, BigDecimal consultationFee) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        if (user.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Tài khoản người dùng này không phải là Bác sĩ!");
        }

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa ID: " + specialtyId));

        // Kích hoạt tài khoản
        user.setIsActive(true);
        userRepository.save(user);

        // Tạo hoặc cập nhật thông tin Doctor tương ứng
        Doctor doctor = doctorRepository.findById(userId).orElse(new Doctor());
        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setExperienceYears(experienceYears != null ? experienceYears : 5);
        doctor.setQualifications(qualifications != null && !qualifications.isBlank() ? qualifications : "Bác sĩ chuyên khoa");
        doctor.setConsultationFee(consultationFee != null ? consultationFee : BigDecimal.valueOf(150000));
        doctorRepository.save(doctor);

        return mapToResponse(user);
    }

    // Cập nhật vai trò (PATIENT <-> DOCTOR). Không cho phép đổi sang ADMIN.
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

        // Defensive Programming: Nếu đổi sang DOCTOR mà tài khoản đang hoạt động, đảm bảo có bản ghi Doctor
        if (user.getRole() == Role.DOCTOR && user.getIsActive()) {
            ensureDoctorRecordExists(user);
        }

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

        // Xóa bác sĩ liên quan nếu có
        doctorRepository.findById(userId).ifPresent(doctorRepository::delete);

        // Xóa profile trước (FK constraint)
        userProfileRepository.findById(userId).ifPresent(userProfileRepository::delete);
        userRepository.delete(user);
    }

    // Đảm bảo bản ghi Doctor luôn tồn tại ở mức cơ sở dữ liệu để tránh lỗi NullPointer ở dashboard bác sĩ
    private void ensureDoctorRecordExists(User user) {
        if (!doctorRepository.existsById(user.getId())) {
            Specialty specialty = specialtyRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bất kỳ chuyên khoa nào để khởi tạo bác sĩ!"));

            Doctor doctor = Doctor.builder()
                    .user(user)
                    .specialty(specialty)
                    .experienceYears(5)
                    .qualifications("Bác sĩ chuyên khoa")
                    .consultationFee(BigDecimal.valueOf(150000))
                    .build();
            doctorRepository.save(doctor);
        }
    }

    // Mapper Entity -> DTO
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

package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.UserResponse;
import com.smarthealthcareplatform.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // Lấy danh sách tất cả người dùng
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // Lấy 1 người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    // Khóa / Mở khóa tài khoản
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleUserActive(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserActive(id));
    }

    // Cập nhật vai trò
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        return ResponseEntity.ok(adminService.updateUserRole(id, newRole));
    }

    // Phê duyệt tài khoản Bác sĩ
    @PutMapping("/{id}/approve-doctor")
    public ResponseEntity<UserResponse> approveDoctor(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long specialtyId = Long.valueOf(body.get("specialtyId").toString());
        Integer experienceYears = Integer.valueOf(body.get("experienceYears").toString());
        String qualifications = body.get("qualifications").toString();
        java.math.BigDecimal consultationFee = new java.math.BigDecimal(body.get("consultationFee").toString());
        return ResponseEntity.ok(adminService.approveDoctor(id, specialtyId, experienceYears, qualifications, consultationFee));
    }

    // Xóa người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("Đã xóa tài khoản thành công.");
    }
}

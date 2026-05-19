package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.MedicalHistoryResponse;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.repository.UserRepository;
import com.smarthealthcareplatform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final UserRepository userRepository;

    // CORE-02 & CORE-07: Chỉ Bệnh nhân mới được xem lịch sử y tế CỦA CHÍNH MÌNH
    @GetMapping("/{patientId}/history")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<MedicalHistoryResponse>> getPatientHistory(
            @PathVariable Long patientId, Authentication authentication) {
        
        // BUG-08 FIX: Kiểm tra quyền sở hữu — bệnh nhân chỉ được xem bệnh án của chính mình
        // Bác sĩ được phép xem bệnh án của mọi bệnh nhân (để chẩn đoán)
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (!isDoctor && !currentUser.getId().equals(patientId)) {
            throw new RuntimeException("Bạn không có quyền xem lịch sử bệnh án của người khác!");
        }

        // Gọi Service lấy lịch sử đã được cấu trúc thành DTO hoàn chỉnh
        return ResponseEntity.ok(patientService.getPatientHistory(patientId));
    }
}

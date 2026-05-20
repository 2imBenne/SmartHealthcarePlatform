package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.MedicalHistoryResponse;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.repository.UserRepository;
import com.smarthealthcareplatform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final UserRepository userRepository;

    // CORE-02 & CORE-07: Patient chi duoc xem lich su cua chinh minh
    @GetMapping("/{patientId}/history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalHistoryResponse>> getPatientHistory(
            @PathVariable Long patientId, Authentication authentication) {

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        if (!currentUser.getId().equals(patientId)) {
            throw new RuntimeException("Bạn không có quyền xem lịch sử bệnh án của người khác!");
        }

        return ResponseEntity.ok(patientService.getPatientHistory(patientId));
    }
}

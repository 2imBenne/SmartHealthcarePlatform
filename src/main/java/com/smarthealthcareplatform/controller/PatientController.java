package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.MedicalHistoryResponse;
import com.smarthealthcareplatform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // CORE-02 & CORE-07: Chỉ Bệnh nhân hoặc Bác sĩ mới được xem lịch sử y tế
    @GetMapping("/{patientId}/history")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<MedicalHistoryResponse>> getPatientHistory(@PathVariable Long patientId) {
        
        // Gọi Service lấy lịch sử đã được cấu trúc thành DTO hoàn chỉnh
        return ResponseEntity.ok(patientService.getPatientHistory(patientId));
    }
}

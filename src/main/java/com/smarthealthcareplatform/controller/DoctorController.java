package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.CreateMedicalRecordRequest;
import com.smarthealthcareplatform.service.MedicalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final MedicalService medicalService;

    // CORE-06: Khám bệnh, nhập triệu chứng, chọn thuốc, lưu kết quả
    @PostMapping("/medical-records")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> createMedicalRecord(@RequestBody CreateMedicalRecordRequest request) {
        medicalService.createMedicalRecordAndPrescription(request);
        return ResponseEntity.ok("Ghi nhận bệnh án và đơn thuốc thành công (Transaction Completed).");
    }
}

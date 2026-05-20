package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.service.MedicalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final MedicalService medicalService;

    // CORE-08: Bac si hoac Admin xac nhan cap phat thuoc
    @PostMapping("/prescriptions/{prescriptionId}/dispense")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<String> dispensePrescription(@PathVariable Long prescriptionId) {
        medicalService.dispensePrescription(prescriptionId);
        return ResponseEntity.ok("Cấp phát thuốc thành công");
    }
}

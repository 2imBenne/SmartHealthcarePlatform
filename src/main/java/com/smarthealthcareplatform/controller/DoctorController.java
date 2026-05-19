package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.CreateMedicalRecordRequest;
import com.smarthealthcareplatform.service.MedicalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final MedicalService medicalService;
    private final com.smarthealthcareplatform.service.AppointmentService appointmentService;

    // BUG-09 FIX: Duyệt lịch hẹn — truyền email bác sĩ để xác minh quyền sở hữu
    @PutMapping("/appointments/{id}/confirm")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> confirmAppointment(@PathVariable Long id, Authentication authentication) {
        String doctorEmail = authentication.getName();
        appointmentService.confirmAppointment(id, doctorEmail);
        return ResponseEntity.ok("Xác nhận lịch khám thành công!");
    }

    // BUG-09 FIX: Tạo bệnh án — truyền email bác sĩ để xác minh quyền sở hữu
    @PostMapping("/medical-records")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> createMedicalRecord(@RequestBody CreateMedicalRecordRequest request, Authentication authentication) {
        String doctorEmail = authentication.getName();
        medicalService.createMedicalRecordAndPrescription(request, doctorEmail);
        return ResponseEntity.ok("Ghi nhận bệnh án và đơn thuốc thành công (Transaction Completed).");
    }
}

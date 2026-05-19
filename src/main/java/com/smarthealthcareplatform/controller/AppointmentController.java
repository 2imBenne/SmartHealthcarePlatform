package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.AppointmentRequest;
import com.smarthealthcareplatform.dto.AppointmentResponse;
import com.smarthealthcareplatform.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller tầng HTTP — chỉ nhận request, gọi Service và trả DTO.
 * KHÔNG trực tiếp chạm vào Entity, Repository hay business logic.
 */
@RestController
@RequestMapping("/api/patient/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // CORE-05: Đặt lịch khám
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @RequestBody AppointmentRequest request,
            Authentication authentication) {
        String patientEmail = authentication.getName();
        AppointmentResponse response = appointmentService.bookAppointment(patientEmail, request);
        return ResponseEntity.ok(response);
    }

    // CORE-09: Hủy lịch & giải phóng Slot
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long id, Authentication authentication) {
        String patientEmail = authentication.getName();
        appointmentService.cancelAppointment(id, patientEmail);
        return ResponseEntity.ok("Hủy lịch thành công! Slot giờ đã được giải phóng.");
    }
}

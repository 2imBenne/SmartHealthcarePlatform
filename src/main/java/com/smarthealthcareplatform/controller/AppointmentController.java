package com.smarthealthcareplatform.controller;

import com.smarthealthcareplatform.dto.AppointmentRequest;
import com.smarthealthcareplatform.entity.Appointment;
import com.smarthealthcareplatform.entity.AppointmentStatus;
import com.smarthealthcareplatform.entity.Doctor;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.repository.DoctorRepository;
import com.smarthealthcareplatform.repository.UserRepository;
import com.smarthealthcareplatform.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    // CORE-05: Đặt lịch khám
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Appointment> bookAppointment(@RequestBody AppointmentRequest request, Authentication authentication) {
        String patientEmail = authentication.getName();
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Patient"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Doctor"));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.PENDING)
                .build();

        return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
    }

    // CORE-09: Hủy lịch
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok("Hủy lịch thành công! Slot giờ đã được giải phóng.");
    }
}

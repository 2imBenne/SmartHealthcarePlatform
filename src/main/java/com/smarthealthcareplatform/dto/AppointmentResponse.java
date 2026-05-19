package com.smarthealthcareplatform.dto;

import com.smarthealthcareplatform.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * DTO trả về cho Client sau khi đặt lịch thành công.
 * Tuân thủ nguyên tắc: KHÔNG phơi bày Entity ra ngoài tầng Controller.
 */
@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientEmail;
    private Long doctorId;
    private String doctorName;
    private String specialtyName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
}

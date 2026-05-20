package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.AppointmentRequest;
import com.smarthealthcareplatform.dto.AppointmentResponse;
import com.smarthealthcareplatform.entity.Appointment;
import com.smarthealthcareplatform.entity.AppointmentStatus;
import com.smarthealthcareplatform.entity.Doctor;
import com.smarthealthcareplatform.entity.User;
import com.smarthealthcareplatform.repository.AppointmentRepository;
import com.smarthealthcareplatform.repository.DoctorRepository;
import com.smarthealthcareplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    // CORE-05: Chống xung đột thời gian (Anti-conflict)
    // Logic nghiệp vụ được đóng gói hoàn toàn ở tầng Service, không để Controller "biết" về Entity
    @Transactional
    public AppointmentResponse bookAppointment(String patientEmail, AppointmentRequest request) {
        // 1. Resolve entities (tầng Service chịu trách nhiệm tìm kiếm Entity)
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản bệnh nhân: " + patientEmail));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể đặt lịch khám cho một ngày trong quá khứ.");
        }

        // Build Entity và lưu (toàn bộ xảy ra trong tầng Service)
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.PENDING)
                .build();

        try {
            appointment = appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Khung giờ này của bác sĩ đã có người đặt, vui lòng chọn giờ khác.");
        }

        // 5. Map Entity -> DTO trước khi trả ra ngoài
        return mapToResponse(appointment);
    }

    // Duyệt và xác nhận lịch hẹn của bệnh nhân
    @Transactional
    public void confirmAppointment(Long appointmentId, String doctorEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + appointmentId));

        // BUG-09 FIX: Kiểm tra bác sĩ chỉ duyệt lịch hẹn của chính mình
        if (!appointment.getDoctor().getUser().getEmail().equals(doctorEmail)) {
            throw new RuntimeException("Bạn không có quyền duyệt lịch khám này!");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xác nhận lịch khám đang ở trạng thái chờ duyệt (PENDING)");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    // CORE-09: Hủy lịch chủ động & Giải phóng Slot (Trước 24h)
    @Transactional
    public void cancelAppointment(Long appointmentId, String patientEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám ID: " + appointmentId));

        // BUG-03 FIX: Kiểm tra quyền sở hữu — bệnh nhân chỉ được hủy lịch của chính mình
        if (!appointment.getPatient().getEmail().equals(patientEmail)) {
            throw new RuntimeException("Bạn không có quyền hủy lịch khám này!");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể hủy lịch đang chờ hoặc đã xác nhận");
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime());
        // Kiểm tra thời gian hiện tại có trước giờ khám 24 tiếng không
        if (LocalDateTime.now().plusHours(24).isAfter(appointmentDateTime)) {
            throw new RuntimeException("Phải hủy lịch trước ít nhất 24 giờ.");
        }

        // Xóa vật lý để giải phóng slot hoàn toàn ở mức Database (tránh vi phạm UniqueConstraint - CORE-09)
        appointmentRepository.delete(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        String doctorName = (a.getDoctor() != null && a.getDoctor().getUser() != null
                && a.getDoctor().getUser().getProfile() != null)
                ? a.getDoctor().getUser().getProfile().getFullName()
                : "N/A";
        String specialtyName = (a.getDoctor() != null && a.getDoctor().getSpecialty() != null)
                ? a.getDoctor().getSpecialty().getName()
                : "N/A";

        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientEmail(a.getPatient().getEmail())
                .doctorId(a.getDoctor() != null ? a.getDoctor().getUserId() : null)
                .doctorName(doctorName)
                .specialtyName(specialtyName)
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .reason(a.getReason())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}

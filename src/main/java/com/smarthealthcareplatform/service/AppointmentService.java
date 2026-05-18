package com.smarthealthcareplatform.service;


import com.smarthealthcareplatform.repository.AppointmentRepository;
import com.smarthealthcareplatform.entity.Appointment;
import com.smarthealthcareplatform.entity.AppointmentStatus;
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

    // CORE-05: Chống xung đột thời gian (Anti-conflict)
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        // 1. Logic mềm: Không cho đặt lịch trong quá khứ
        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể đặt lịch khám cho một ngày trong quá khứ.");
        }

        // 2. Logic cứng (Bắt lỗi từ Database)
        // Khi 2 người đặt cùng 1 bác sĩ, cùng 1 giờ, UNIQUE KEY ở DB sẽ vi phạm
        // Spring Data JPA sẽ ném ra DataIntegrityViolationException
        try {
            return appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Khung giờ này của bác sĩ đã có người đặt, vui lòng chọn giờ khác.");
        }
    }

    // CORE-09: Hủy lịch chủ động & Giải phóng Slot (Trước 24h)
    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám"));

        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể hủy lịch đang chờ hoặc đã xác nhận");
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime());
        // Kiểm tra thời gian hiện tại có trước giờ khám 24 tiếng không
        if (LocalDateTime.now().plusHours(24).isAfter(appointmentDateTime)) {
            throw new RuntimeException("Phải hủy lịch trước ít nhất 24 giờ.");
        }

        // Xóa vật lý cuộc hẹn để giải phóng slot hoàn toàn ở mức Database (tránh vi phạm UniqueConstraint)
        appointmentRepository.delete(appointment);
    }
}

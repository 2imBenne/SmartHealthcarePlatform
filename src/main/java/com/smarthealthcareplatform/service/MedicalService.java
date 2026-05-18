package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.CreateMedicalRecordRequest;
import com.smarthealthcareplatform.dto.PrescriptionItemRequest;
import com.smarthealthcareplatform.entity.*;
import com.smarthealthcareplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalService {
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final MedicineRepository medicineRepository;

    // CORE-06: Đảm bảo ACID với @Transactional
    @Transactional(rollbackFor = Exception.class)
    public void createMedicalRecordAndPrescription(CreateMedicalRecordRequest request) {
        // 1. Kiểm tra Lịch khám
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám"));
        
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ được tạo bệnh án cho lịch khám đã xác nhận (CONFIRMED)");
        }

        // 2. Tạo Bệnh Án (Medical Record)
        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .advice(request.getAdvice())
                .build();
        record = medicalRecordRepository.save(record);

        // 3. Nếu có kê đơn thuốc, tạo Đơn Thuốc (Prescription)
        if (request.getPrescriptionItems() != null && !request.getPrescriptionItems().isEmpty()) {
            Prescription prescription = Prescription.builder()
                    .medicalRecord(record)
                    .status(PrescriptionStatus.PENDING)
                    .build();
            prescription = prescriptionRepository.save(prescription);

            // 4. Lưu chi tiết đơn thuốc (Thuộc CORE-06)
            for (PrescriptionItemRequest item : request.getPrescriptionItems()) {
                Medicine medicine = medicineRepository.findByIdAndIsActiveTrue(item.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc hoặc thuốc đã ngừng bán"));

                // KHÔNG trừ tồn kho ở bước này (Tách sang CORE-08). Lưu chi tiết đơn thuốc kèm GIÁ LỊCH SỬ

                PrescriptionDetail detail = PrescriptionDetail.builder()
                        .prescription(prescription)
                        .medicine(medicine)
                        .quantity(item.getQuantity())
                        .dosageInstructions(item.getDosageInstructions())
                        .unitPrice(medicine.getPrice()) // Lấy giá hiện tại lưu vào lịch sử
                        .build();
                prescriptionDetailRepository.save(detail);
            }
        }

        // 5. Hoàn tất - Đổi trạng thái lịch khám thành COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    // CORE-08: Cấp phát thuốc & Quản lý Tồn kho (Tách biệt khỏi CORE-06)
    @Transactional(rollbackFor = Exception.class)
    public void dispensePrescription(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc"));

        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new RuntimeException("Đơn thuốc này không ở trạng thái Chờ cấp phát");
        }

        // Lấy danh sách chi tiết đơn thuốc
        var details = prescriptionDetailRepository.findByPrescriptionId(prescriptionId);

        for (PrescriptionDetail detail : details) {
            // Lock row này lại (Pessimistic Lock) để chống bán lố thuốc
            Medicine medicine = medicineRepository.findByIdAndIsActiveTrue(detail.getMedicine().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc: " + detail.getMedicine().getName()));

            if (medicine.getStockQuantity() < detail.getQuantity()) {
                throw new RuntimeException("Không đủ tồn kho cho thuốc: " + medicine.getName() + ". Tồn hiện tại: " + medicine.getStockQuantity());
            }

            // Trừ lùi tồn kho
            medicine.setStockQuantity(medicine.getStockQuantity() - detail.getQuantity());
            medicineRepository.save(medicine);
        }

        // Cập nhật trạng thái đơn thuốc
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        prescriptionRepository.save(prescription);
    }
}

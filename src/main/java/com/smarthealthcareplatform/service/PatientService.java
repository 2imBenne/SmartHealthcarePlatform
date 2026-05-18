package com.smarthealthcareplatform.service;

import com.smarthealthcareplatform.dto.MedicalHistoryResponse;
import com.smarthealthcareplatform.dto.MedicineDetailDTO;
import com.smarthealthcareplatform.entity.MedicalRecord;
import com.smarthealthcareplatform.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final MedicalRecordRepository medicalRecordRepository;

    @Transactional(readOnly = true)
    public List<MedicalHistoryResponse> getPatientHistory(Long patientId) {
        // CORE-07: Truy vấn toàn bộ dữ liệu chỉ bằng 1 câu lệnh SQL duy nhất nhờ JOIN FETCH
        List<MedicalRecord> records = medicalRecordRepository.findHistoryByPatientId(patientId);

        // Map từ Entity sang DTO để trả về cho Frontend, tuyệt đối không lộ Entity ra ngoài
        return records.stream().map(record -> {
            List<MedicineDetailDTO> medicineDTOs = new ArrayList<>();
            
            // Xử lý danh sách chi tiết thuốc nếu bác sĩ có kê đơn
            if (record.getPrescription() != null && record.getPrescription().getDetails() != null) {
                medicineDTOs = record.getPrescription().getDetails().stream().map(detail ->
                        MedicineDetailDTO.builder()
                                .medicineName(detail.getMedicine().getName())
                                .unit(detail.getMedicine().getUnit())
                                .quantity(detail.getQuantity())
                                .dosageInstructions(detail.getDosageInstructions())
                                .unitPrice(detail.getUnitPrice())
                                .build()
                ).collect(Collectors.toList());
            }

            return MedicalHistoryResponse.builder()
                    .recordId(record.getId())
                    .appointmentDate(record.getAppointment().getAppointmentDate())
                    .doctorName(record.getAppointment().getDoctor().getUser().getProfile().getFullName())
                    .specialtyName(record.getAppointment().getDoctor().getSpecialty().getName())
                    .symptoms(record.getSymptoms())
                    .diagnosis(record.getDiagnosis())
                    .advice(record.getAdvice())
                    .medicines(medicineDTOs)
                    .build();
        }).collect(Collectors.toList());
    }
}

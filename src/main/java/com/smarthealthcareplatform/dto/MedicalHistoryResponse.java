package com.smarthealthcareplatform.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MedicalHistoryResponse {
    private Long recordId;
    private LocalDate appointmentDate;
    private String doctorName;
    private String specialtyName;
    private String symptoms;
    private String diagnosis;
    private String advice;
    private List<MedicineDetailDTO> medicines;
}

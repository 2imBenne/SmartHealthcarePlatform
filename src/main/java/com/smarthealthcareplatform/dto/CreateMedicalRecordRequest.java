package com.smarthealthcareplatform.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateMedicalRecordRequest {
    private Long appointmentId;
    private String symptoms;
    private String diagnosis;
    private String advice;
    // DTO con chứa danh sách thuốc kê đơn
    private List<PrescriptionItemRequest> prescriptionItems;
}

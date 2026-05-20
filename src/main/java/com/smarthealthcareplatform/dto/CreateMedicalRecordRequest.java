package com.smarthealthcareplatform.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateMedicalRecordRequest {
    private Long appointmentId;
    private String symptoms;
    private String diagnosis;
    private String advice;

    private List<PrescriptionItemRequest> prescriptionItems;
}

package com.smarthealthcareplatform.dto;

import lombok.Data;

@Data
public class PrescriptionItemRequest {
    private Long medicineId;
    private Integer quantity;
    private String dosageInstructions;
}

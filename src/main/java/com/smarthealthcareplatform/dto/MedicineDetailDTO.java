package com.smarthealthcareplatform.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MedicineDetailDTO {
    private String medicineName;
    private String unit;
    private Integer quantity;
    private String dosageInstructions;
    private BigDecimal unitPrice;
}

package com.smarthealthcareplatform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedicineRequest {
    private String name;
    private String unit;
    private Integer stockQuantity;
    private BigDecimal price;
    private Boolean isActive;
}

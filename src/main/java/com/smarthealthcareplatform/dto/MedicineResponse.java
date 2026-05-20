package com.smarthealthcareplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {
    private Long id;
    private String name;
    private String unit;
    private Integer stockQuantity;
    private BigDecimal price;
    private Boolean isActive;
}

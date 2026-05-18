package com.smarthealthcareplatform.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileRequest {
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String address;
}

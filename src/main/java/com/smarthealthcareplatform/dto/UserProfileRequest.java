package com.smarthealthcareplatform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileRequest {
    private String fullName;

    @NotNull(message = "Ngay sinh khong duoc de trong")
    @Past(message = "Ngay sinh phai la ngay trong qua khu")
    private LocalDate dateOfBirth;

    private String gender;
    private String phoneNumber;
    private String address;
}

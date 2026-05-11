package com.medical.doctorplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdateRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}

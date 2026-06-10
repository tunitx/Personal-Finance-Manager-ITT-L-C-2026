package com.tunitx.PRM.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must be at least 8 characters with one uppercase, one number, one special character"
    )
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
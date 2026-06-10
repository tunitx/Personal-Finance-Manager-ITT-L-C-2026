package com.tunitx.PRM.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must be at least 8 characters with one uppercase, one number, one special character"
    )
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "^(MANAGER|EMPLOYEE|ADMIN)$",
            message = "Role must be MANAGER or EMPLOYEE or ADMIN" //remove admin from here later
    )
    private String role;
}
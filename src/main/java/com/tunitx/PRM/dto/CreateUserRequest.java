package com.tunitx.PRM.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Temporary password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must be at least 8 chars, 1 uppercase, 1 number, 1 special"
    )
    private String temporaryPassword;

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "^(ADMIN|MANAGER|EMPLOYEE)$",
            message = "Role must be ADMIN, MANAGER or EMPLOYEE"
    )
    private String role;

    // optional profile fields
    private String department;
    private String designation;
}
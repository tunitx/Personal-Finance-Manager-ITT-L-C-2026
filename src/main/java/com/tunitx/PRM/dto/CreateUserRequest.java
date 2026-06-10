package com.tunitx.PRM.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "^(MANAGER|EMPLOYEE|ADMIN)$",
            message = "Role must be MANAGER or EMPLOYEE or ADMIN"
    )
    private String role;

}

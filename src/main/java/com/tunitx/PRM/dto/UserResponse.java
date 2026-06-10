package com.tunitx.PRM.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private String email;
    private boolean isActive;
    private boolean forcePasswordChange;
}

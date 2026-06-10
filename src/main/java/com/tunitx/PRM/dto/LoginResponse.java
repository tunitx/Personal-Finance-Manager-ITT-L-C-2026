package com.tunitx.PRM.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private String role;
    private String fullName;
    private boolean forcePasswordChange;
}
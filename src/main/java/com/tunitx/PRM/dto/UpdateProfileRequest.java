package com.tunitx.PRM.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    private String fullName;
    private String email;
    private String department;
    private String designation;
}

package com.tunitx.PRM.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private String designation;
    private String status;
    private boolean isActive;
    private List<SkillResponse> skills;
}
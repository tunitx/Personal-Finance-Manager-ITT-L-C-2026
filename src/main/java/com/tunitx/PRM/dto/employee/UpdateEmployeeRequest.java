package com.tunitx.PRM.dto.employee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeRequest {

    private String department;
    private String designation;
}
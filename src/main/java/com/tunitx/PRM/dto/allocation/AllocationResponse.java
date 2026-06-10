package com.tunitx.PRM.dto.allocation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AllocationResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long projectId;
    private String projectName;
    private Integer utilisationPct;
    private LocalDate fromDate;
    private LocalDate toDate;
    private boolean isActive;
}
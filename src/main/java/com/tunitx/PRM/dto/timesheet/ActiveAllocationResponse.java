package com.tunitx.PRM.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ActiveAllocationResponse {

    private Long projectId;
    private String projectName;
    private Integer utilisationPct;
    private LocalDate fromDate;
    private LocalDate toDate;
}
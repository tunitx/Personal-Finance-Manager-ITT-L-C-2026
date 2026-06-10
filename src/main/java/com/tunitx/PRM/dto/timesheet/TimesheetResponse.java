package com.tunitx.PRM.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class TimesheetResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate weekStart;
    private String status;
    private Integer totalHours;
    private LocalDateTime submittedAt;
    private List<TimesheetEntryResponse> entries;
}
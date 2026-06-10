package com.tunitx.PRM.dto.timesheet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TimesheetEntryRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Hours logged is required")
    @Min(value = 1, message = "Hours must be at least 1")
    private Integer hoursLogged;

    private List<Long> tagIds;
}
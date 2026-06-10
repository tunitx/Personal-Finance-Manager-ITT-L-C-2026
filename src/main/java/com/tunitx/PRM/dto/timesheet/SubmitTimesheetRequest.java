package com.tunitx.PRM.dto.timesheet;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SubmitTimesheetRequest {

    @NotNull(message = "Week start date is required")
    private LocalDate weekStart;

    @NotEmpty(message = "At least one entry is required")
    private List<TimesheetEntryRequest> entries;
}
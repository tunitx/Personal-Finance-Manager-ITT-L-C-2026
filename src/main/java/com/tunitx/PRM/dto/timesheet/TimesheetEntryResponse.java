package com.tunitx.PRM.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TimesheetEntryResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private Integer hoursLogged;
    private List<TagResponse> tags;
}
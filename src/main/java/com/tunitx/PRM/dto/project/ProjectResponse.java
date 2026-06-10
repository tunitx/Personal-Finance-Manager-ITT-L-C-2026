package com.tunitx.PRM.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long managerId;
    private String managerName;
    private List<MilestoneResponse> milestones;
}
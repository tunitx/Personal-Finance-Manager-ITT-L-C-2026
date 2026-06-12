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

    // BRD Screen 4.3 — health and risk flags
    private String health;                          // AT_RISK | ON_TRACK | ATTENTION
    private List<String> riskFlags;                 // plain-English risk descriptions

    // BRD Screen 4.3 — allocated resources list
    private List<AllocationInProjectResponse> allocations;
}

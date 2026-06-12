package com.tunitx.PRM.dto;

import com.tunitx.PRM.dto.allocation.AllocationResponse;
import com.tunitx.PRM.dto.employee.SkillResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailResponse {

    private Long id;
    private String fullName;
    private String department;
    private String designation;
    private String status;
    private Integer utilisationPct;
    private Long managerId;           // ← new
    private String managerName;       // total current utilisation %
    private List<SkillResponse> skills;
    private List<AllocationResponse> activeAllocations;
    private List<String> recentActivityTags;     // last 4 weeks
}

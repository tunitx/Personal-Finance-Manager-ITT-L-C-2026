package com.tunitx.PRM.dto.project;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMilestoneRequest {

    @Pattern(
            regexp = "^(NOT_STARTED|IN_PROGRESS|DONE|OVERDUE)$",
            message = "Status must be NOT_STARTED, IN_PROGRESS, DONE or OVERDUE"
    )
    private String status;

    private String title;
}
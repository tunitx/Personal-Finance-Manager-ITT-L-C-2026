package com.tunitx.PRM.dto.project;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProjectRequest {

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    @Pattern(
            regexp = "^(PLANNED|ACTIVE|ON_HOLD|COMPLETED)$",
            message = "Status must be PLANNED, ACTIVE, ON_HOLD or COMPLETED"
    )
    private String status;

    private Long managerId;
}
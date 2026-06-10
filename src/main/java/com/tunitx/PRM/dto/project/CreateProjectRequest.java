package com.tunitx.PRM.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "^(PLANNED|ACTIVE|ON_HOLD|COMPLETED)$",
            message = "Status must be PLANNED, ACTIVE, ON_HOLD or COMPLETED"
    )
    private String status;

    @NotNull(message = "Manager ID is required")
    private Long managerId;
}
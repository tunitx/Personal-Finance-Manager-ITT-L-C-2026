package com.tunitx.PRM.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillMatchRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Requirement description is required")
    private String requirement;
}
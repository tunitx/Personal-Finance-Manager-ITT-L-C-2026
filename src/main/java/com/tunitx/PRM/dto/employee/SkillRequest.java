package com.tunitx.PRM.dto.employee;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Proficiency is required")
    @Pattern(
            regexp = "^(BEGINNER|INTERMEDIATE|ADVANCED)$",
            message = "Proficiency must be BEGINNER, INTERMEDIATE or ADVANCED"
    )
    private String proficiency;
}
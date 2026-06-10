package com.tunitx.PRM.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSkillRequest {

    @NotBlank(message = "Skill name is required")
    private String name;

    @NotBlank(message = "Category is required")
    @Pattern(
            regexp = "^(BACKEND|FRONTEND|DEVOPS|QA|OTHER)$",
            message = "Category must be BACKEND, FRONTEND, DEVOPS, QA or OTHER"
    )
    private String category;
}
package com.tunitx.PRM.dto.ai;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamBuildRequest {

    @NotNull
    private Long projectId;

    @NotEmpty(message = "At least one role is required")
    private List<RoleRequirement> roles;

    @Getter
    @Setter
    public static class RoleRequirement {

        @NotNull
        private String roleName;           // e.g. "Senior Java Developer"

        @NotEmpty
        private List<String> requiredSkills; // e.g. ["Java", "Spring Boot"]

        private String minProficiency;     // BEGINNER | INTERMEDIATE | ADVANCED
    }
}

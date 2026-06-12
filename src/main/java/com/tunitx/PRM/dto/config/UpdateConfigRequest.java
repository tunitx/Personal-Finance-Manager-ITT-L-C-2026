package com.tunitx.PRM.dto.config;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateConfigRequest {

    @Pattern(
            regexp = "^(GEMINI|GROQ)$",
            message = "Provider must be GEMINI or GROQ"
    )
    private String llmProvider;

    private String llmApiKey;

    private Integer schedulerIntervalHrs;

    private Integer maxWeeklyHours;
}
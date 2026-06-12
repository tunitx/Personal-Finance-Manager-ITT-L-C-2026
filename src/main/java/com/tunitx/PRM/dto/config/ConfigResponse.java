package com.tunitx.PRM.dto.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfigResponse {

    private String llmProvider;
    private String llmApiKeyMasked;
    private Integer schedulerIntervalHrs;
    private Integer maxWeeklyHours;
}
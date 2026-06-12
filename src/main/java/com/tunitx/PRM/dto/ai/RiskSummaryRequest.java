package com.tunitx.PRM.dto.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskSummaryRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;
}
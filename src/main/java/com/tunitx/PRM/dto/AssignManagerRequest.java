package com.tunitx.PRM.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignManagerRequest {

    @NotNull(message = "Manager ID is required")
    private Long managerId;
}
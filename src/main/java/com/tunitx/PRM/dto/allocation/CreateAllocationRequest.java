package com.tunitx.PRM.dto.allocation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateAllocationRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Utilisation percentage is required")
    @Min(value = 1, message = "Utilisation must be at least 1%")
    @Max(value = 100, message = "Utilisation cannot exceed 100%")
    private Integer utilisationPct;

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;
}
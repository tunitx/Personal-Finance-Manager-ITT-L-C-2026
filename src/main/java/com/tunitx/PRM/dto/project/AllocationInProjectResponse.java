package com.tunitx.PRM.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AllocationInProjectResponse {

    private Long userId;
    private String userName;
    private Integer utilisationPct;
    private LocalDate fromDate;
    private LocalDate toDate;
}

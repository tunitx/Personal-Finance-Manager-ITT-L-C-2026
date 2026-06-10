package com.tunitx.PRM.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class MilestoneResponse {

    private Long id;
    private String title;
    private LocalDate dueDate;
    private String status;
}
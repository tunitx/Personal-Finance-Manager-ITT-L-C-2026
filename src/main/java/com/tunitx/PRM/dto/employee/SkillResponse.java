package com.tunitx.PRM.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkillResponse {

    private Long skillId;
    private String name;
    private String category;
    private String proficiency;
}
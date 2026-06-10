package com.tunitx.PRM.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EmployeeSkillId implements Serializable {

    private Long employeeId;
    private Long skillId;
}
package com.tunitx.PRM.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkill {

    @EmbeddedId
    private EmployeeSkillId id;

    @ManyToOne
    @MapsId("employeeId")
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @MapsId("skillId")
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false, length = 20)
    private String proficiency;
}
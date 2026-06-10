package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.EmployeeSkill;
import com.tunitx.PRM.model.EmployeeSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSkillRepository
        extends JpaRepository<EmployeeSkill, EmployeeSkillId> {

    List<EmployeeSkill> findByEmployeeId(Long employeeId);
}
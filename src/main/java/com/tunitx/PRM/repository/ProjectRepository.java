package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByManagerId(Long managerId);

    List<Project> findByStatus(String status);
}
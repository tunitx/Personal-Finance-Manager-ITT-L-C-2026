package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    boolean existsByName(String name);

    Optional<Skill> findByName(String name);
}
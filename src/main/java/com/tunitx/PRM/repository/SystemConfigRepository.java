package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    default SystemConfig getConfig() {
        return findById(1L).orElseThrow(() ->
                new RuntimeException("System config not found"));
    }
}
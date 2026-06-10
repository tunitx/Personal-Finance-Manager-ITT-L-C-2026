package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByEmployeeId(Long employeeId);

    List<Allocation> findByProjectId(Long projectId);

    List<Allocation> findByEmployeeIdAndIsActive(Long employeeId, boolean isActive);

    @Query("""
                SELECT COALESCE(SUM(a.utilisationPct), 0)
                FROM Allocation a
                WHERE a.employee.id = :employeeId
                AND a.isActive = true
                AND a.fromDate <= :toDate
                AND a.toDate >= :fromDate
                AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    Integer getTotalUtilisationForPeriod(
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("excludeId") Long excludeId
    );
}
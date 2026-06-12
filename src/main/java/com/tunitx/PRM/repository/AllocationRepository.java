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


    List<Allocation> findByUserId(Long userId);

    List<Allocation> findByUserIdAndIsActive(Long userId, boolean isActive);

    List<Allocation> findByProjectId(Long projectId);


    @Query("""
                SELECT COALESCE(SUM(a.utilisationPct), 0)
                FROM Allocation a
                WHERE a.user.Id = :userId
                AND a.isActive = true
                AND a.fromDate <= :toDate
                AND a.toDate >= :fromDate
                AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    Integer getTotalUtilisationForPeriod(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("excludeId") Long excludeId
    );
}
package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findByUserId(Long userId);

    Optional<Timesheet> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);

    List<Timesheet> findByUserIdIn(List<Long> userIds);

    List<Timesheet> findByUserIdInAndWeekStart(List<Long> userIds, LocalDate weekStart);
}
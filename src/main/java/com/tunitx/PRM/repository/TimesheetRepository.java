package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findByEmployeeId(Long employeeId);

    Optional<Timesheet> findByEmployeeIdAndWeekStart(
            Long employeeId, LocalDate weekStart);

    List<Timesheet> findByEmployeeIdIn(List<Long> employeeIds);

    List<Timesheet> findByEmployeeIdInAndWeekStart(
            List<Long> employeeIds, LocalDate weekStart);
}
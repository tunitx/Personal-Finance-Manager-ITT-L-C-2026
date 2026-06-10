package com.tunitx.PRM.scheduler;

import com.tunitx.PRM.model.Allocation;
import com.tunitx.PRM.model.Employee;
import com.tunitx.PRM.model.Milestone;
import com.tunitx.PRM.model.Timesheet;
import com.tunitx.PRM.repository.AllocationRepository;
import com.tunitx.PRM.repository.EmployeeRepository;
import com.tunitx.PRM.repository.MilestoneRepository;
import com.tunitx.PRM.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final EmployeeRepository employeeRepository;
    private final TimesheetRepository timesheetRepository;
    private final MilestoneRepository milestoneRepository;
    private final AllocationRepository allocationRepository;

    @Scheduled(fixedDelayString = "${scheduler.interval.ms:14400000}")
    @Transactional
    public void runScheduledJobs() {
        log.info("Scheduler running at {}", LocalDate.now());
        flagMissedTimesheets();
        updateOverdueMilestones();
    }

    private void flagMissedTimesheets() {

        LocalDate lastMonday = LocalDate.now()
                .with(TemporalAdjusters.previous(DayOfWeek.MONDAY));

        log.info("Checking missed timesheets for week starting {}", lastMonday);

        List<Employee> activeEmployees =
                employeeRepository.findByIsActive(true);

        for (Employee employee : activeEmployees) {

            List<Allocation> activeAllocations = allocationRepository
                    .findByEmployeeIdAndIsActive(employee.getId(), true);

            if (activeAllocations.isEmpty()) {
                continue;
            }

            Optional<Timesheet> existing = timesheetRepository
                    .findByEmployeeIdAndWeekStart(
                            employee.getId(), lastMonday);

            if (existing.isEmpty()) {
                Timesheet missed = new Timesheet();
                missed.setEmployee(employee);
                missed.setWeekStart(lastMonday);
                missed.setStatus("MISSED");
                timesheetRepository.save(missed);

                log.info("Marked timesheet as MISSED for employee {} week {}",
                        employee.getFullName(), lastMonday);
            }
        }
    }

    private void updateOverdueMilestones() {

        log.info("Checking overdue milestones");

        List<Milestone> milestones = milestoneRepository.findAll();

        for (Milestone milestone : milestones) {

            if (milestone.getStatus().equals("DONE")) {
                continue;
            }

            if (milestone.getDueDate().isBefore(LocalDate.now())
                    && !milestone.getStatus().equals("OVERDUE")) {

                milestone.setStatus("OVERDUE");
                milestoneRepository.save(milestone);

                log.info("Marked milestone '{}' as OVERDUE",
                        milestone.getTitle());
            }
        }
    }
}
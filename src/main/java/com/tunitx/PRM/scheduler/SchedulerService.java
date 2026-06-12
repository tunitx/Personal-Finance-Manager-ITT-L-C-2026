package com.tunitx.PRM.scheduler;

import com.tunitx.PRM.model.*;
import com.tunitx.PRM.repository.*;
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

    private final UserRepository userRepository;           // ← was EmployeeRepository
    private final TimesheetRepository timesheetRepository;
    private final MilestoneRepository milestoneRepository;
    private final AllocationRepository allocationRepository;
    private final ProjectRepository projectRepository;

    @Scheduled(fixedDelayString = "${scheduler.interval.ms:14400000}")
    @Transactional
    public void runScheduledJobs() {
        log.info("Scheduler running at {}", LocalDate.now());
        flagMissedTimesheets();
        updateOverdueMilestones();
        expireAllocationsAndUpdateStatus();
        updateProjectHealth();
    }

    private void expireAllocationsAndUpdateStatus() {

        log.info("Checking expired allocations");

        LocalDate today = LocalDate.now();

        // Find all active allocations whose to_date has passed
        List<Allocation> expiredAllocations = allocationRepository
                .findAll()
                .stream()
                .filter(a -> a.isActive() && a.getToDate().isBefore(today))
                .collect(java.util.stream.Collectors.toList());

        for (Allocation allocation : expiredAllocations) {

            // End the allocation
            allocation.setActive(false);
            allocationRepository.save(allocation);

            log.info("Auto-expired allocation: {} on project {} (ended {})",
                    allocation.getUser().getFullName(),
                    allocation.getProject().getName(),
                    allocation.getToDate());
        }

        // Recompute BENCH/ALLOCATED status for all non-admin users
        List<User> activeUsers = userRepository.findByIsActive(true);

        for (User user : activeUsers) {

            if (user.getRole().getName().equals("ADMIN")) continue;

            // Skip users with no profile (status still null)
            if (user.getStatus() == null) continue;

            boolean hasActiveAllocation = allocationRepository
                    .findByUserIdAndIsActive(user.getId(), true)
                    .stream()
                    .anyMatch(a -> !a.getFromDate().isAfter(today)
                            && !a.getToDate().isBefore(today));

            String correctStatus = hasActiveAllocation ? "ALLOCATED" : "BENCH";

            if (!correctStatus.equals(user.getStatus())) {
                user.setStatus(correctStatus);
                userRepository.save(user);
                log.info("Updated {} status: {} → {}",
                        user.getFullName(),
                        user.getStatus(),
                        correctStatus);
            }
        }
    }

    private void flagMissedTimesheets() {

        LocalDate lastMonday = LocalDate.now()
                .with(TemporalAdjusters.previous(DayOfWeek.MONDAY));

        log.info("Checking missed timesheets for week starting {}", lastMonday);

        List<User> activeUsers = userRepository.findByIsActive(true);  // ← was findByIsActive on employeeRepository

        for (User user : activeUsers) {                                // ← was Employee employee

            // skip ADMIN users — they don't submit timesheets
            if (user.getRole().getName().equals("ADMIN")) {            // ← new check, no equivalent before
                continue;
            }

            List<Allocation> activeAllocations = allocationRepository
                    .findByUserIdAndIsActive(user.getId(), true);      // ← was findByEmployeeIdAndIsActive

            if (activeAllocations.isEmpty()) {
                continue;
            }

            Optional<Timesheet> existing = timesheetRepository
                    .findByUserIdAndWeekStart(                         // ← was findByEmployeeIdAndWeekStart
                            user.getId(), lastMonday);

            if (existing.isEmpty()) {
                Timesheet missed = new Timesheet();
                missed.setUser(user);                                  // ← was setEmployee
                missed.setWeekStart(lastMonday);
                missed.setStatus("MISSED");
                timesheetRepository.save(missed);

                log.info("Marked timesheet as MISSED for user {} week {}",
                        user.getFullName(), lastMonday);               // ← was employee.getFullName()
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

    private void updateProjectHealth() {
        log.info("Updating project health flags");
        LocalDate today = LocalDate.now();

        projectRepository.findAll().forEach(project -> {
            List<Milestone> milestones = milestoneRepository
                    .findByProjectId(project.getId());

            List<Allocation> allocations = allocationRepository
                    .findByProjectId(project.getId());

            boolean hasOverdue = milestones.stream()
                    .anyMatch(m -> "OVERDUE".equals(m.getStatus()));

            boolean lowEffort = allocations.stream().anyMatch(a -> {
                int expected = (a.getUtilisationPct() * 40) / 100;
                if (expected == 0) return false;

                // sum hours logged on this project last week
                LocalDate lastMonday = today.with(
                        TemporalAdjusters.previous(DayOfWeek.MONDAY));

                int logged = timesheetRepository
                        .findByUserId(a.getUser().getId())
                        .stream()
                        .filter(t -> t.getWeekStart().equals(lastMonday))
                        .flatMap(t -> t.getEntries() == null
                                ? java.util.stream.Stream.empty()
                                : t.getEntries().stream())
                        .filter(e -> e.getProject().getId()
                                .equals(project.getId()))
                        .mapToInt(TimesheetEntry::getHoursLogged)
                        .sum();

                return logged < (expected / 2);
            });

            String health;
            if (hasOverdue || lowEffort) {
                health = "AT_RISK";
            } else if (project.getEndDate().isBefore(today.plusWeeks(2))) {
                health = "ATTENTION";
            } else {
                health = "ON_TRACK";
            }

            project.setHealth(health);
            projectRepository.save(project);
            log.info("Project '{}' health → {}", project.getName(), health);
        });
    }
}
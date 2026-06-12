package com.tunitx.PRM.scheduler;

import com.tunitx.PRM.dto.ai.RiskSummaryRequest;
import com.tunitx.PRM.model.*;
import com.tunitx.PRM.repository.*;
import com.tunitx.PRM.service.AiService;
import com.tunitx.PRM.service.EmailService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final UserRepository userRepository;
    private final TimesheetRepository timesheetRepository;
    private final MilestoneRepository milestoneRepository;
    private final AllocationRepository allocationRepository;
    private final ProjectRepository projectRepository;
    private final UserManagerMappingRepository userManagerMappingRepository;
    private final EmailService emailService;
    private final AiService aiService;

    @Scheduled(fixedDelayString = "${scheduler.interval.ms:14400000}")
    @Transactional
    public void runScheduledJobs() {
        log.info("Scheduler running at {}", LocalDate.now());
        flagMissedTimesheets();
        updateOverdueMilestones();
        expireAllocationsAndUpdateStatus();
        updateProjectHealth();
    }

    // ── Job 1 — Flag missed timesheets + send reminders + freeze ──────────

    private void flagMissedTimesheets() {
        LocalDate lastMonday = LocalDate.now()
                .with(TemporalAdjusters.previous(DayOfWeek.MONDAY));

        log.info("Checking missed timesheets for week starting {}", lastMonday);

        List<User> activeUsers = userRepository.findByIsActive(true);

        for (User user : activeUsers) {

            if (user.getRole().getName().equals("ADMIN")) continue;

            List<Allocation> activeAllocations = allocationRepository
                    .findByUserIdAndIsActive(user.getId(), true);

            if (activeAllocations.isEmpty()) continue;

            Optional<Timesheet> existing = timesheetRepository
                    .findByUserIdAndWeekStart(user.getId(), lastMonday);

            if (existing.isEmpty()) {
                // Create MISSED record if not already there
                boolean alreadyMissed = timesheetRepository
                        .findByUserId(user.getId()).stream()
                        .anyMatch(t -> t.getWeekStart().equals(lastMonday)
                                && "MISSED".equals(t.getStatus()));

                if (!alreadyMissed) {
                    Timesheet missed = new Timesheet();
                    missed.setUser(user);
                    missed.setWeekStart(lastMonday);
                    missed.setStatus("MISSED");
                    timesheetRepository.save(missed);
                    log.info("Marked timesheet MISSED for {} week {}",
                            user.getFullName(), lastMonday);
                }

                // Reminder & freeze logic
                handleTimesheetReminders(user, lastMonday);
            }
        }
    }

    private void handleTimesheetReminders(User user, LocalDate weekStart) {
        LocalDate today = LocalDate.now();

        // Only send one reminder per day
        if (user.getLastReminderSentAt() != null
                && user.getLastReminderSentAt().equals(today)) {
            return;
        }

        int count = user.getReminderCount();

        if (count == 0) {
            // Reminder 1 — first working day after deadline (Monday)
            emailService.sendTimesheetReminder(user, weekStart, 1);
            user.setReminderCount(1);
            user.setLastReminderSentAt(today);
            userRepository.save(user);
            log.info("Sent reminder 1 to {}", user.getUsername());

        } else if (count == 1) {
            // Reminder 2 — next working day
            emailService.sendTimesheetReminder(user, weekStart, 2);
            user.setReminderCount(2);
            user.setLastReminderSentAt(today);
            userRepository.save(user);
            log.info("Sent reminder 2 to {}", user.getUsername());

        } else if (count >= 2 && !user.isTimesheetFrozen()) {
            // Freeze after 2 reminders
            user.setTimesheetFrozen(true);
            userRepository.save(user);

            // Get manager
            User manager = userManagerMappingRepository
                    .findByUserId(user.getId())
                    .map(UserManagerMapping::getManager)
                    .orElse(null);

            emailService.sendTimesheetFreezeNotice(user, manager, weekStart);
            log.info("Frozen timesheet access for {}", user.getUsername());
        }
    }

    // ── Job 2 — Update overdue milestones ─────────────────────────────────

    private void updateOverdueMilestones() {
        log.info("Checking overdue milestones");

        milestoneRepository.findAll().forEach(milestone -> {
            if ("DONE".equals(milestone.getStatus())) return;
            if (milestone.getDueDate().isBefore(LocalDate.now())
                    && !"OVERDUE".equals(milestone.getStatus())) {
                milestone.setStatus("OVERDUE");
                milestoneRepository.save(milestone);
                log.info("Marked milestone '{}' as OVERDUE",
                        milestone.getTitle());
            }
        });
    }

    // ── Job 3 — Expire allocations + recompute BENCH/ALLOCATED ───────────

    private void expireAllocationsAndUpdateStatus() {
        log.info("Checking expired allocations");
        LocalDate today = LocalDate.now();

        allocationRepository.findAll().stream()
                .filter(a -> a.isActive() && a.getToDate().isBefore(today))
                .forEach(a -> {
                    a.setActive(false);
                    allocationRepository.save(a);
                    log.info("Auto-expired allocation: {} on {}",
                            a.getUser().getFullName(),
                            a.getProject().getName());
                });

        userRepository.findByIsActive(true).stream()
                .filter(u -> !u.getRole().getName().equals("ADMIN"))
                .filter(u -> u.getStatus() != null)
                .forEach(user -> {
                    boolean hasActive = allocationRepository
                            .findByUserIdAndIsActive(user.getId(), true)
                            .stream()
                            .anyMatch(a -> !a.getFromDate().isAfter(today)
                                    && !a.getToDate().isBefore(today));

                    String correct = hasActive ? "ALLOCATED" : "BENCH";
                    if (!correct.equals(user.getStatus())) {
                        user.setStatus(correct);
                        userRepository.save(user);
                        log.info("Status updated: {} → {}",
                                user.getFullName(), correct);
                    }
                });
    }

    // ── Job 4 — Update project health + send AT_RISK alerts ───────────────

    private void updateProjectHealth() {
        log.info("Updating project health flags");
        LocalDate today = LocalDate.now();

        projectRepository.findAll().forEach(project -> {

            String previousHealth = project.getHealth();

            List<Milestone> milestones = milestoneRepository
                    .findByProjectId(project.getId());
            List<Allocation> allocations = allocationRepository
                    .findByProjectId(project.getId());

            boolean hasOverdue = milestones.stream()
                    .anyMatch(m -> "OVERDUE".equals(m.getStatus()));

            boolean lowEffort = allocations.stream().anyMatch(a -> {
                int expected = (a.getUtilisationPct() * 40) / 100;
                if (expected == 0) return false;

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

            String newHealth;
            if (hasOverdue || lowEffort) {
                newHealth = "AT_RISK";
            } else if (project.getEndDate().isBefore(today.plusWeeks(2))) {
                newHealth = "ATTENTION";
            } else {
                newHealth = "ON_TRACK";
            }

            project.setHealth(newHealth);
            projectRepository.save(project);
            log.info("Project '{}' health → {}", project.getName(), newHealth);

            // Send alert only when health CHANGES to AT_RISK
            if ("AT_RISK".equals(newHealth)
                    && !"AT_RISK".equals(previousHealth)) {
                sendAtRiskAlert(project);
            }
        });
    }

    private void sendAtRiskAlert(Project project) {
        User manager = project.getManager();
        if (manager == null) return;

        // Get AI risk summary
        String riskSummary;
        try {
            RiskSummaryRequest req = new RiskSummaryRequest();
            req.setProjectId(project.getId());
            riskSummary = aiService.riskSummary(req).getResult();
        } catch (Exception e) {
            riskSummary = "Unable to generate AI summary at this time.";
            log.warn("AI risk summary failed for at-risk alert: {}",
                    e.getMessage());
        }

        // Get bench employees as suggested help
        String suggestedHelp = userRepository.findByStatus("BENCH")
                .stream()
                .filter(u -> !u.getRole().getName().equals("ADMIN"))
                .map(u -> {
                    String skills = u.getFullName() + " — "
                            + (u.getDesignation() != null
                            ? u.getDesignation() : "—");
                    return skills;
                })
                .collect(Collectors.joining("<br/>"));

        if (suggestedHelp.isBlank()) {
            suggestedHelp = "No employees currently on bench.";
        }

        emailService.sendProjectAtRiskAlert(
                project, manager, riskSummary, suggestedHelp);
    }
}

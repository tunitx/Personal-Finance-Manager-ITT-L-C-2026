package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.project.*;
import com.tunitx.PRM.model.*;
import com.tunitx.PRM.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;
    private final AllocationRepository allocationRepository;
    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;

    public List<ProjectResponse> getAllProjects(Long managerId) {
        List<Project> projects = managerId != null
                ? projectRepository.findByManagerId(managerId)
                : projectRepository.findAll();

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToResponse(project);
    }

    public ProjectResponse getProject(Long id, Long managerId) {
        Project project = projectRepository.findByIdAndManagerId(id, managerId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToResponse(project);
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new RuntimeException("End date must be after start date");

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!manager.getRole().getName().equals("MANAGER"))
            throw new RuntimeException("Assigned user is not a Manager");

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setManager(manager);

        return mapToResponse(projectRepository.save(project));
    }

    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
        if (request.getStatus() != null) project.setStatus(request.getStatus());

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            if (!manager.getRole().getName().equals("MANAGER"))
                throw new RuntimeException("Assigned user is not a Manager");
            project.setManager(manager);
        }

        return mapToResponse(projectRepository.save(project));
    }

    public List<MilestoneResponse> getMilestones(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return milestoneRepository.findByProjectId(projectId)
                .stream().map(this::mapMilestoneToResponse)
                .collect(Collectors.toList());
    }

    public MilestoneResponse createMilestone(Long projectId,
                                             CreateMilestoneRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setTitle(request.getTitle());
        milestone.setDueDate(request.getDueDate());
        milestone.setStatus("NOT_STARTED");
        return mapMilestoneToResponse(milestoneRepository.save(milestone));
    }

    public MilestoneResponse updateMilestone(Long projectId, Long milestoneId,
                                             UpdateMilestoneRequest request) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        if (request.getTitle() != null) milestone.setTitle(request.getTitle());
        if (request.getStatus() != null) milestone.setStatus(request.getStatus());
        return mapMilestoneToResponse(milestoneRepository.save(milestone));
    }

    // ── Health computation ─────────────────────────────────────────────────

    /**
     * Computes project health and risk flags.
     * AT RISK   — any OVERDUE milestone OR any allocated user with
     * significantly below-expected hours last week
     * ATTENTION — project end date approaching within 2 weeks, or
     * IN_PROGRESS milestone close to overdue
     * ON TRACK  — everything else
     */
    private String computeHealth(Project project,
                                 List<Milestone> milestones, List<Allocation> allocations) {

        boolean hasOverdue = milestones.stream()
                .anyMatch(m -> "OVERDUE".equals(m.getStatus()));

        boolean lowEffort = allocations.stream().anyMatch(a -> {
            int expected = (a.getUtilisationPct() * 40) / 100;
            int logged = getHoursLastWeek(a.getUser().getId(), project.getId());
            return expected > 0 && logged < (expected / 2); // less than half expected
        });

        if (hasOverdue || lowEffort) return "AT_RISK";

        boolean approaching = project.getEndDate()
                .isBefore(LocalDate.now().plusWeeks(2));
        boolean inProgressClose = milestones.stream()
                .anyMatch(m -> "IN_PROGRESS".equals(m.getStatus())
                        && m.getDueDate().isBefore(LocalDate.now().plusDays(3)));

        if (approaching || inProgressClose) return "ATTENTION";

        return "ON_TRACK";
    }

    private List<String> computeRiskFlags(Project project,
                                          List<Milestone> milestones, List<Allocation> allocations) {

        List<String> flags = new ArrayList<>();

        milestones.stream()
                .filter(m -> "OVERDUE".equals(m.getStatus()))
                .forEach(m -> flags.add(m.getTitle() + " milestone is overdue"));

        allocations.forEach(a -> {
            int expected = (a.getUtilisationPct() * 40) / 100;
            int logged = getHoursLastWeek(a.getUser().getId(), project.getId());
            if (expected > 0 && logged < (expected / 2)) {
                flags.add(a.getUser().getFullName() + " logged only "
                        + logged + " hrs last week (expected " + expected + " hrs)");
            }
        });

        if (flags.isEmpty()) {
            flags.add("Resources are correctly allocated");
        }

        return flags;
    }

    private int getHoursLastWeek(Long userId, Long projectId) {
        LocalDate lastMonday = LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters
                        .previous(java.time.DayOfWeek.MONDAY));

        return timesheetRepository.findByUserId(userId).stream()
                .filter(t -> t.getWeekStart().equals(lastMonday))
                .flatMap(t -> timesheetEntryRepository
                        .findByTimesheetId(t.getId()).stream())
                .filter(e -> e.getProject().getId().equals(projectId))
                .mapToInt(TimesheetEntry::getHoursLogged)
                .sum();
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private ProjectResponse mapToResponse(Project project) {
        List<MilestoneResponse> milestones = milestoneRepository
                .findByProjectId(project.getId())
                .stream().map(this::mapMilestoneToResponse)
                .collect(Collectors.toList());

        List<Allocation> allocations = allocationRepository
                .findByProjectId(project.getId());

        List<AllocationInProjectResponse> allocationResponses = allocations.stream()
                .map(a -> new AllocationInProjectResponse(
                        a.getUser().getId(),
                        a.getUser().getFullName(),
                        a.getUtilisationPct(),
                        a.getFromDate(),
                        a.getToDate()))
                .collect(Collectors.toList());

        List<Milestone> milestoneEntities = milestoneRepository
                .findByProjectId(project.getId());

        String health = computeHealth(project, milestoneEntities, allocations);
        List<String> riskFlags = computeRiskFlags(project,
                milestoneEntities, allocations);

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getManager().getId(),
                project.getManager().getFullName(),
                milestones,
                health,
                riskFlags,
                allocationResponses
        );
    }

    private MilestoneResponse mapMilestoneToResponse(Milestone milestone) {
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getTitle(),
                milestone.getDueDate(),
                milestone.getStatus()
        );
    }
}

package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.project.*;
import com.tunitx.PRM.model.Employee;
import com.tunitx.PRM.model.Milestone;
import com.tunitx.PRM.model.Project;
import com.tunitx.PRM.repository.EmployeeRepository;
import com.tunitx.PRM.repository.MilestoneRepository;
import com.tunitx.PRM.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final EmployeeRepository employeeRepository;

    public List<ProjectResponse> getAllProjects(Long managerId) {
        List<Project> projects;

        if (managerId != null) {
            projects = projectRepository.findByManagerId(managerId);
        } else {
            projects = projectRepository.findAll();
        }

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return mapToResponse(project);
    }

    public ProjectResponse createProject(CreateProjectRequest request) {

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        Employee manager = employeeRepository.findById(request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setManager(manager);

        Project saved = projectRepository.save(project);
        return mapToResponse(saved);
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
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            project.setManager(manager);
        }

        Project saved = projectRepository.save(project);
        return mapToResponse(saved);
    }

    public List<MilestoneResponse> getMilestones(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return milestoneRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapMilestoneToResponse)
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

        Milestone saved = milestoneRepository.save(milestone);
        return mapMilestoneToResponse(saved);
    }

    public MilestoneResponse updateMilestone(Long projectId, Long milestoneId,
                                             UpdateMilestoneRequest request) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        if (request.getTitle() != null) milestone.setTitle(request.getTitle());
        if (request.getStatus() != null) milestone.setStatus(request.getStatus());

        Milestone saved = milestoneRepository.save(milestone);
        return mapMilestoneToResponse(saved);
    }

    private ProjectResponse mapToResponse(Project project) {
        List<MilestoneResponse> milestones = milestoneRepository
                .findByProjectId(project.getId())
                .stream()
                .map(this::mapMilestoneToResponse)
                .collect(Collectors.toList());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getManager().getId(),
                project.getManager().getFullName(),
                milestones
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
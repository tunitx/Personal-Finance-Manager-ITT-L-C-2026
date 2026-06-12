package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.project.*;
import com.tunitx.PRM.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) Long managerId) {
        return ResponseEntity.ok(projectService.getAllProjects(managerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @GetMapping("/{id}/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id, @PathVariable Long managerId) {
        return ResponseEntity.ok(projectService.getProject(id, managerId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @GetMapping("/{projectId}/milestones")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<MilestoneResponse>> getMilestones(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getMilestones(projectId));
    }

    @PostMapping("/{projectId}/milestones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MilestoneResponse> createMilestone(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createMilestone(projectId, request));
    }

    @PutMapping("/{projectId}/milestones/{milestoneId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MilestoneResponse> updateMilestone(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId,
            @Valid @RequestBody UpdateMilestoneRequest request) {
        return ResponseEntity.ok(
                projectService.updateMilestone(projectId, milestoneId, request));
    }
}
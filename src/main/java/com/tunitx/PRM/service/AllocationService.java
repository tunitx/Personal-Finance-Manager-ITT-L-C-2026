package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.allocation.AllocationResponse;
import com.tunitx.PRM.dto.allocation.CreateAllocationRequest;
import com.tunitx.PRM.model.Allocation;
import com.tunitx.PRM.model.Project;
import com.tunitx.PRM.model.User;
import com.tunitx.PRM.repository.AllocationRepository;
import com.tunitx.PRM.repository.ProjectRepository;
import com.tunitx.PRM.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final UserRepository userRepository;          // ← was EmployeeRepository
    private final ProjectRepository projectRepository;

    public List<AllocationResponse> getAllAllocations(Long userId, Long projectId) {

        List<Allocation> allocations;

        if (userId != null) {
            allocations = allocationRepository.findByUserId(userId);       // ← was findByEmployeeId
        } else if (projectId != null) {
            allocations = allocationRepository.findByProjectId(projectId);
        } else {
            allocations = allocationRepository.findAll();
        }

        return allocations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AllocationResponse> getMyAllocations(Long userId) {
        return allocationRepository.findByUserId(userId)                   // ← was findByEmployeeId
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AllocationResponse createAllocation(CreateAllocationRequest request) {

        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new RuntimeException("To date must be after from date");
        }

        User user = userRepository.findById(request.getUserId())           // ← was employeeRepository + Employee
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getStatus().equals("ACTIVE")
                && !project.getStatus().equals("PLANNED")) {
            throw new RuntimeException(
                    "Cannot allocate to a project that is not ACTIVE or PLANNED");
        }

        Integer currentUtilisation = allocationRepository
                .getTotalUtilisationForPeriod(
                        request.getUserId(),                               // ← was getEmployeeId
                        request.getFromDate(),
                        request.getToDate(),
                        null
                );

        int newTotal = currentUtilisation + request.getUtilisationPct();
        if (newTotal > 100) {
            throw new RuntimeException(
                    "Over-allocation detected. Current utilisation in this period: "
                            + currentUtilisation + "%. Adding "
                            + request.getUtilisationPct()
                            + "% would result in " + newTotal + "%."
            );
        }

        Allocation allocation = new Allocation();
        allocation.setUser(user);                                          // ← was setEmployee
        allocation.setProject(project);
        allocation.setUtilisationPct(request.getUtilisationPct());
        allocation.setFromDate(request.getFromDate());
        allocation.setToDate(request.getToDate());
        allocation.setActive(true);

        if (newTotal > 0) {
            user.setStatus("ALLOCATED");
            userRepository.save(user);                                     // ← was employeeRepository
        }

        Allocation saved = allocationRepository.save(allocation);
        return mapToResponse(saved);
    }

    public AllocationResponse endAllocation(Long allocationId) {

        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new RuntimeException("Allocation not found"));

        if (!allocation.isActive()) {
            throw new RuntimeException("Allocation is already ended");
        }

        allocation.setActive(false);
        allocation.setToDate(LocalDate.now());
        allocationRepository.save(allocation);

        List<Allocation> activeAllocations = allocationRepository
                .findByUserIdAndIsActive(                                  // ← was findByEmployeeIdAndIsActive
                        allocation.getUser().getId(), true);               // ← was getEmployee()

        if (activeAllocations.isEmpty()) {
            User user = allocation.getUser();                              // ← was getEmployee()
            user.setStatus("BENCH");
            userRepository.save(user);                                     // ← was employeeRepository
        }

        return mapToResponse(allocation);
    }

    private AllocationResponse mapToResponse(Allocation allocation) {
        return new AllocationResponse(
                allocation.getId(),
                allocation.getUser().getId(),                              // ← was getEmployee()
                allocation.getUser().getFullName(),                        // ← was getEmployee()
                allocation.getProject().getId(),
                allocation.getProject().getName(),
                allocation.getUtilisationPct(),
                allocation.getFromDate(),
                allocation.getToDate(),
                allocation.isActive()
        );
    }
}
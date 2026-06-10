package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.allocation.AllocationResponse;
import com.tunitx.PRM.dto.allocation.CreateAllocationRequest;
import com.tunitx.PRM.model.Allocation;
import com.tunitx.PRM.model.Employee;
import com.tunitx.PRM.model.Project;
import com.tunitx.PRM.repository.AllocationRepository;
import com.tunitx.PRM.repository.EmployeeRepository;
import com.tunitx.PRM.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public List<AllocationResponse> getAllAllocations(
            Long employeeId, Long projectId) {

        List<Allocation> allocations;

        if (employeeId != null) {
            allocations = allocationRepository.findByEmployeeId(employeeId);
        } else if (projectId != null) {
            allocations = allocationRepository.findByProjectId(projectId);
        } else {
            allocations = allocationRepository.findAll();
        }

        return allocations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AllocationResponse> getMyAllocations(Long employeeId) {
        return allocationRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AllocationResponse createAllocation(CreateAllocationRequest request) {

        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new RuntimeException("To date must be after from date");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getStatus().equals("ACTIVE")
                && !project.getStatus().equals("PLANNED")) {
            throw new RuntimeException(
                    "Cannot allocate to a project that is not ACTIVE or PLANNED");
        }

        Integer currentUtilisation = allocationRepository
                .getTotalUtilisationForPeriod(
                        request.getEmployeeId(),
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
        allocation.setEmployee(employee);
        allocation.setProject(project);
        allocation.setUtilisationPct(request.getUtilisationPct());
        allocation.setFromDate(request.getFromDate());
        allocation.setToDate(request.getToDate());
        allocation.setActive(true);

        if (newTotal > 0) {
            employee.setStatus("ALLOCATED");
            employeeRepository.save(employee);
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
                .findByEmployeeIdAndIsActive(
                        allocation.getEmployee().getId(), true);

        if (activeAllocations.isEmpty()) {
            Employee employee = allocation.getEmployee();
            employee.setStatus("BENCH");
            employeeRepository.save(employee);
        }

        return mapToResponse(allocation);
    }

    private AllocationResponse mapToResponse(Allocation allocation) {
        return new AllocationResponse(
                allocation.getId(),
                allocation.getEmployee().getId(),
                allocation.getEmployee().getFullName(),
                allocation.getProject().getId(),
                allocation.getProject().getName(),
                allocation.getUtilisationPct(),
                allocation.getFromDate(),
                allocation.getToDate(),
                allocation.isActive()
        );
    }
}
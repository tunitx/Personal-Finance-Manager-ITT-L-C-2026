package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.allocation.AllocationResponse;
import com.tunitx.PRM.dto.allocation.CreateAllocationRequest;
import com.tunitx.PRM.model.Employee;
import com.tunitx.PRM.repository.EmployeeRepository;
import com.tunitx.PRM.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;
    private final EmployeeRepository employeeRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AllocationResponse>> getAllAllocations(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(
                allocationService.getAllAllocations(employeeId, projectId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AllocationResponse>> getMyAllocations(
            Authentication authentication) {

        String username = authentication.getName();
        Employee employee = employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        return ResponseEntity.ok(
                allocationService.getMyAllocations(employee.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AllocationResponse> createAllocation(
            @Valid @RequestBody CreateAllocationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(allocationService.createAllocation(request));
    }

    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AllocationResponse> endAllocation(
            @PathVariable Long id) {
        return ResponseEntity.ok(allocationService.endAllocation(id));
    }
}
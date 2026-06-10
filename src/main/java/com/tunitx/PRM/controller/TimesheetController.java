package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.timesheet.ActiveAllocationResponse;
import com.tunitx.PRM.dto.timesheet.SubmitTimesheetRequest;
import com.tunitx.PRM.dto.timesheet.TagResponse;
import com.tunitx.PRM.dto.timesheet.TimesheetResponse;
import com.tunitx.PRM.model.Employee;
import com.tunitx.PRM.repository.EmployeeRepository;
import com.tunitx.PRM.service.TimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/tags")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(timesheetService.getAllTags());
    }

    @GetMapping("/my/active-allocations")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<ActiveAllocationResponse>> getActiveAllocations(
            @RequestParam LocalDate weekStart,
            Authentication authentication) {

        Employee employee = getEmployee(authentication);
        return ResponseEntity.ok(
                timesheetService.getActiveAllocations(
                        employee.getId(), weekStart));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TimesheetResponse>> getMyTimesheets(
            Authentication authentication) {

        Employee employee = getEmployee(authentication);
        return ResponseEntity.ok(
                timesheetService.getMyTimesheets(employee.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TimesheetResponse> submitTimesheet(
            @Valid @RequestBody SubmitTimesheetRequest request,
            Authentication authentication) {

        Employee employee = getEmployee(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timesheetService.submitTimesheet(
                        employee.getId(), request));
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TimesheetResponse>> getTeamTimesheets(
            @RequestParam Long managerId,
            @RequestParam(required = false) LocalDate weekStart) {
        return ResponseEntity.ok(
                timesheetService.getTeamTimesheets(managerId, weekStart));
    }

    private Employee getEmployee(Authentication authentication) {
        String username = authentication.getName();
        return employeeRepository.findByUserUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Employee profile not found"));
    }
}
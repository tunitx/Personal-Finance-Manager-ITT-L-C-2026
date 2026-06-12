package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.timesheet.ActiveAllocationResponse;
import com.tunitx.PRM.dto.timesheet.SubmitTimesheetRequest;
import com.tunitx.PRM.dto.timesheet.TagResponse;
import com.tunitx.PRM.dto.timesheet.TimesheetResponse;
import com.tunitx.PRM.model.User;
import com.tunitx.PRM.repository.UserRepository;
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
    private final UserRepository userRepository;          // ← was EmployeeRepository

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

        User user = getUser(authentication);              // ← was getEmployee
        return ResponseEntity.ok(
                timesheetService.getActiveAllocations(
                        user.getId(), weekStart));        // ← was employee.getId()
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TimesheetResponse>> getMyTimesheets(
            Authentication authentication) {

        User user = getUser(authentication);              // ← was getEmployee
        return ResponseEntity.ok(
                timesheetService.getMyTimesheets(user.getId()));  // ← was employee.getId()
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TimesheetResponse> submitTimesheet(
            @Valid @RequestBody SubmitTimesheetRequest request,
            Authentication authentication) {

        User user = getUser(authentication);              // ← was getEmployee
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timesheetService.submitTimesheet(
                        user.getId(), request));          // ← was employee.getId()
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TimesheetResponse>> getTeamTimesheets(
            @RequestParam Long managerId,
            @RequestParam(required = false) LocalDate weekStart) {
        return ResponseEntity.ok(
                timesheetService.getTeamTimesheets(managerId, weekStart));
    }

    private User getUser(Authentication authentication) { // ← was getEmployee returning Employee
        String username = authentication.getName();
        return userRepository.findByUsername(username)    // ← was employeeRepository.findByUserUsername
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}
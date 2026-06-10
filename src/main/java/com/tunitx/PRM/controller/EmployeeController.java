package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.employee.*;
import com.tunitx.PRM.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(employeeService.getAllEmployees(status, department));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok("Employee deactivated.");
    }

    @GetMapping("/{id}/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SkillResponse>> getEmployeeSkills(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeSkills(id));
    }

    @PostMapping("/{id}/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request) {
        employeeService.addSkill(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Skill added.");
    }

    @PutMapping("/{id}/skills/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSkill(
            @PathVariable Long id,
            @PathVariable Long skillId,
            @Valid @RequestBody SkillRequest request) {
        employeeService.updateSkillProficiency(id, skillId, request);
        return ResponseEntity.ok("Proficiency updated.");
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> removeSkill(
            @PathVariable Long id,
            @PathVariable Long skillId) {
        employeeService.removeSkill(id, skillId);
        return ResponseEntity.ok("Skill removed.");
    }

    @GetMapping("/skills")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(employeeService.getAllSkills());
    }

    @PostMapping("/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillResponse> createSkill(
            @Valid @RequestBody CreateSkillRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeService.createSkill(request));
    }
}
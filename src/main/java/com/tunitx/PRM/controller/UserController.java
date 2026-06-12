package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.*;
import com.tunitx.PRM.dto.employee.CreateSkillRequest;
import com.tunitx.PRM.dto.employee.SkillRequest;
import com.tunitx.PRM.dto.employee.SkillResponse;
import com.tunitx.PRM.service.UserManagerMappingService;
import com.tunitx.PRM.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserManagerMappingService userManagerMappingService;

    // ── User CRUD ──────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(userService.getAllUsers(status, department));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(
                "Password reset. User will be prompted to change it on next login.");
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User deactivated.");
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reactivateUser(@PathVariable Long id) {
        userService.reactivateUser(id);
        return ResponseEntity.ok("User reactivated.");
    }

    // ── Employee detail (for manager drill-down BRD Screen 4.1) ───────────

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetail(id));
    }

    // ── Skills on user (moved from EmployeeController) ────────────────────

    @GetMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SkillResponse>> getUserSkills(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserSkills(id));
    }

    @PostMapping("/{id}/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request) {
        userService.addSkill(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Skill added.");
    }

    @PutMapping("/{id}/skills/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSkill(
            @PathVariable Long id,
            @PathVariable Long skillId,
            @Valid @RequestBody SkillRequest request) {
        userService.updateSkillProficiency(id, skillId, request);
        return ResponseEntity.ok("Proficiency updated.");
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> removeSkill(
            @PathVariable Long id,
            @PathVariable Long skillId) {
        userService.removeSkill(id, skillId);
        return ResponseEntity.ok("Skill removed.");
    }

    // ── Skills catalogue ───────────────────────────────────────────────────

    @GetMapping("/skills")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(userService.getAllSkills());
    }

    @PostMapping("/skills")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillResponse> createSkill(
            @Valid @RequestBody CreateSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createSkill(request));
    }

    @PutMapping("/{id}/assign-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignManager(
            @PathVariable Long id,
            @RequestBody AssignManagerRequest request) {
        userManagerMappingService.assignManager(id, request.getManagerId());
        return ResponseEntity.ok("Manager assigned.");
    }

    @DeleteMapping("/{id}/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> removeManager(@PathVariable Long id) {
        userManagerMappingService.removeManager(id);
        return ResponseEntity.ok("Manager removed.");
    }
}

package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.CreateUserRequest;
import com.tunitx.PRM.dto.ResetPasswordRequest;
import com.tunitx.PRM.dto.UserResponse;
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
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<String> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok("Password reset. User will be prompted to change it on next login.");
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User deactivated.");
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<String> reactivateUser(@PathVariable Long id) {
        userService.reactivateUser(id);
        return ResponseEntity.ok("User reactivated.");
    }
}
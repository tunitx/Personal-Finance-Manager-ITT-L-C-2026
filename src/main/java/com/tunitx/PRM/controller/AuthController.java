package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.ChangePasswordRequest;
import com.tunitx.PRM.dto.LoginRequest;
import com.tunitx.PRM.dto.LoginResponse;
import com.tunitx.PRM.dto.SignUpRequest;
import com.tunitx.PRM.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// generic routes, used by manager, employee to create their accounts and reset their password
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(
            @Valid @RequestBody SignUpRequest request) {

        authService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Account created successfully. Please log in.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        authService.changePassword(username, request);
        return ResponseEntity.ok("Password changed successfully.");
    }
}
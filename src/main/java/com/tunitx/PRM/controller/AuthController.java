package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.ChangePasswordRequest;
import com.tunitx.PRM.dto.LoginRequest;
import com.tunitx.PRM.dto.LoginResponse;
import com.tunitx.PRM.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // NOTE: /signup removed per BRD V3
    // All accounts are created by Admin via POST /api/users

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        authService.changePassword(username, request);
        return ResponseEntity.ok("Password changed successfully.");
    }
}

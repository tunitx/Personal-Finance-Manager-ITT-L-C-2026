package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.ChangePasswordRequest;
import com.tunitx.PRM.dto.LoginRequest;
import com.tunitx.PRM.dto.LoginResponse;
import com.tunitx.PRM.dto.SignUpRequest;
import com.tunitx.PRM.model.Role;
import com.tunitx.PRM.model.User;
import com.tunitx.PRM.repository.RoleRepository;
import com.tunitx.PRM.repository.UserRepository;
import com.tunitx.PRM.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated. Contact your administrator.");
        }

//        if (user.isForcePasswordChange()) {
//            throw new RuntimeException("Password change required. Please change your password.");
//        }

//        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
//            throw new RuntimeException("Invalid username or password");
//        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRoleName()
        );

        String fullName = user.getUsername();

        return new LoginResponse(
                token,
                user.getId(),
                user.getRoleName(),
                fullName,
                user.isForcePasswordChange()
        );
    }

    public void signUp(SignUpRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));


        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setForcePasswordChange(true);
        user.setActive(true);

        userRepository.save(user);
    }

    public void changePassword(String username, ChangePasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordChange(false);

        userRepository.save(user);
    }
}
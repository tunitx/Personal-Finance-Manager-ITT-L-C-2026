package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.CreateUserRequest;
import com.tunitx.PRM.dto.ResetPasswordRequest;
import com.tunitx.PRM.dto.UserResponse;
import com.tunitx.PRM.model.User;
import com.tunitx.PRM.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getTemporaryPassword()));
        user.setRole(request.getRole());
        user.setForcePasswordChange(true);
        user.setActive(true);

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    public void resetPassword(Long userId, ResetPasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewTemporaryPassword()));
        user.setForcePasswordChange(true);

        userRepository.save(user);
    }

    public void deactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("User is already deactivated");
        }

        user.setActive(false);
        userRepository.save(user);
    }

    public void reactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isActive()) {
            throw new RuntimeException("User is already active");
        }

        user.setActive(true);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isForcePasswordChange()
        );
    }
}
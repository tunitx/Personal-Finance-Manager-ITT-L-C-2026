package com.tunitx.PRM.service;

import com.tunitx.PRM.model.User;
import com.tunitx.PRM.model.UserManagerMapping;
import com.tunitx.PRM.repository.UserManagerMappingRepository;
import com.tunitx.PRM.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManagerMappingService {

    private final UserManagerMappingRepository userManagerMappingRepository;
    private final UserRepository userRepository;

    public void assignManager(Long userId, Long managerId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!manager.getRole().getName().equals("MANAGER")) {
            throw new RuntimeException(
                    "User " + manager.getUsername() + " is not a MANAGER");
        }

        // One user → one manager — upsert the mapping
        Optional<UserManagerMapping> existing =
                userManagerMappingRepository.findByUserId(userId);

        if (existing.isPresent()) {
            existing.get().setManager(manager);
            userManagerMappingRepository.save(existing.get());
        } else {
            UserManagerMapping mapping = new UserManagerMapping();
            mapping.setUserId(userId);
            mapping.setUser(user);
            mapping.setManager(manager);
            userManagerMappingRepository.save(mapping);
        }
    }

    public void removeManager(Long userId) {
        userManagerMappingRepository.findByUserId(userId)
                .ifPresent(userManagerMappingRepository::delete);
    }

    public Long getManagerId(Long userId) {
        return userManagerMappingRepository.findByUserId(userId)
                .map(m -> m.getManager().getId())
                .orElse(null);
    }
}
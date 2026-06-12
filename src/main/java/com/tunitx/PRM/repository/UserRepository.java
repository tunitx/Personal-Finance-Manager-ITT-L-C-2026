package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRoleName(String roleName);

    List<User> findByStatus(String status);

    List<User> findByDepartment(String department);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(@NotBlank(message = "Username is required") String username);

    List<User> findByIsActive(boolean b);
}

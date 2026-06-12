package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.UserManagerMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserManagerMappingRepository
        extends JpaRepository<UserManagerMapping, Long> {

    Optional<UserManagerMapping> findByUserId(Long userId);

    List<UserManagerMapping> findByManagerId(Long managerId);
}
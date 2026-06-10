package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByStatus(String status);

    List<Employee> findByDepartment(String department);

    List<Employee> findByIsActive(boolean isActive);

    Optional<Employee> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Optional<Employee> findByUserUsername(String username);
}
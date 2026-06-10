package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.employee.*;
import com.tunitx.PRM.model.*;
import com.tunitx.PRM.repository.EmployeeRepository;
import com.tunitx.PRM.repository.EmployeeSkillRepository;
import com.tunitx.PRM.repository.SkillRepository;
import com.tunitx.PRM.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    public List<EmployeeResponse> getAllEmployees(String status, String department) {
        List<Employee> employees;

        if (status != null) {
            employees = employeeRepository.findByStatus(status);
        } else if (department != null) {
            employees = employeeRepository.findByDepartment(department);
        } else {
            employees = employeeRepository.findAll();
        }

        return employees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapToResponse(employee);
    }

    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (employeeRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Employee profile already exists for this user");
        }

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setStatus("BENCH");
        employee.setActive(true);

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (request.getDepartment() != null) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getDesignation() != null) {
            employee.setDesignation(request.getDesignation());
        }

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    public void deactivateEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.isActive()) {
            throw new RuntimeException("Employee is already deactivated");
        }

        employee.setActive(false);
        employee.setStatus("BENCH");

        User user = employee.getUser();
        user.setActive(false);
        userRepository.save(user);

        employeeRepository.save(employee);
    }

    // Skills management

    public List<SkillResponse> getEmployeeSkills(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return employeeSkillRepository.findByEmployeeId(employeeId)
                .stream()
                .map(es -> new SkillResponse(
                        es.getSkill().getId(),
                        es.getSkill().getName(),
                        es.getSkill().getCategory(),
                        es.getProficiency()
                ))
                .collect(Collectors.toList());
    }

    public void addSkill(Long employeeId, SkillRequest request) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        EmployeeSkillId esId = new EmployeeSkillId(employeeId, skill.getId());

        if (employeeSkillRepository.existsById(esId)) {
            throw new RuntimeException("Skill already assigned to this employee");
        }

        EmployeeSkill employeeSkill = new EmployeeSkill();
        employeeSkill.setId(esId);
        employeeSkill.setEmployee(employee);
        employeeSkill.setSkill(skill);
        employeeSkill.setProficiency(request.getProficiency());

        employeeSkillRepository.save(employeeSkill);
    }

    public void updateSkillProficiency(Long employeeId, Long skillId, SkillRequest request) {

        EmployeeSkillId esId = new EmployeeSkillId(employeeId, skillId);

        EmployeeSkill employeeSkill = employeeSkillRepository.findById(esId)
                .orElseThrow(() -> new RuntimeException("Skill not assigned to this employee"));

        employeeSkill.setProficiency(request.getProficiency());
        employeeSkillRepository.save(employeeSkill);
    }

    public void removeSkill(Long employeeId, Long skillId) {

        EmployeeSkillId esId = new EmployeeSkillId(employeeId, skillId);

        if (!employeeSkillRepository.existsById(esId)) {
            throw new RuntimeException("Skill not assigned to this employee");
        }

        employeeSkillRepository.deleteById(esId);
    }

    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getCategory(), null))
                .collect(Collectors.toList());
    }

    public SkillResponse createSkill(CreateSkillRequest request) {

        if (skillRepository.existsByName(request.getName())) {
            throw new RuntimeException("Skill already exists");
        }

        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());

        Skill saved = skillRepository.save(skill);
        return new SkillResponse(saved.getId(), saved.getName(), saved.getCategory(), null);
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        List<SkillResponse> skills = employeeSkillRepository
                .findByEmployeeId(employee.getId())
                .stream()
                .map(es -> new SkillResponse(
                        es.getSkill().getId(),
                        es.getSkill().getName(),
                        es.getSkill().getCategory(),
                        es.getProficiency()
                ))
                .collect(Collectors.toList());

        return new EmployeeResponse(
                employee.getId(),
                employee.getUser().getId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getStatus(),
                employee.isActive(),
                skills
        );
    }
}
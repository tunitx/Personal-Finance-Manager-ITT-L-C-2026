package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.*;
import com.tunitx.PRM.dto.allocation.AllocationResponse;
import com.tunitx.PRM.dto.employee.CreateSkillRequest;
import com.tunitx.PRM.dto.employee.SkillRequest;
import com.tunitx.PRM.dto.employee.SkillResponse;
import com.tunitx.PRM.model.*;
import com.tunitx.PRM.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSkillRepository employeeSkillRepository;
    private final SkillRepository skillRepository;
    private final AllocationRepository allocationRepository;
    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final UserManagerMappingRepository userManagerMappingRepository;


    // ── List users with optional filters ──────────────────────────────────

    public void unfreezeTimesheet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTimesheetFrozen()) {
            throw new RuntimeException("Timesheet access is not frozen for this user");
        }

        user.setTimesheetFrozen(false);
        user.setReminderCount(0);
        user.setLastReminderSentAt(null);
        userRepository.save(user);

        log.info("Timesheet access restored for user {}", user.getUsername());
    }

    public List<UserResponse> getAllUsers(String status, String department) {
        List<User> users;

        if (status != null) {
            users = userRepository.findByStatus(status);
        } else if (department != null) {
            users = userRepository.findByDepartment(department);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    // ── BRD Screen 4.1 [D] — full employee detail for manager drill-down ──

    public UserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Skills
        List<SkillResponse> skills = employeeSkillRepository
                .findByUserId(id)
                .stream()
                .map(es -> new SkillResponse(
                        es.getSkill().getId(),
                        es.getSkill().getName(),
                        es.getSkill().getCategory(),
                        es.getProficiency()))
                .collect(Collectors.toList());

        // Active allocations
        List<Allocation> active = allocationRepository
                .findByUserIdAndIsActive(id, true);

        int totalPct = active.stream()
                .mapToInt(Allocation::getUtilisationPct).sum();

        List<AllocationResponse> activeAllocations = active.stream()
                .map(a -> new AllocationResponse(
                        a.getId(),
                        a.getUser().getId(),
                        a.getUser().getFullName(),
                        a.getProject().getId(),
                        a.getProject().getName(),
                        a.getUtilisationPct(),
                        a.getFromDate(),
                        a.getToDate(),
                        a.isActive()))
                .collect(Collectors.toList());

        // Recent activity tags — last 4 weeks
        List<String> recentTags = timesheetRepository
                .findByUserId(id)
                .stream()
                .flatMap(t -> timesheetEntryRepository
                        .findByTimesheetId(t.getId()).stream())
                .flatMap(e -> e.getTags() == null
                        ? java.util.stream.Stream.empty()
                        : e.getTags().stream())
                .map(ActivityTag::getName)
                .distinct()
                .collect(Collectors.toList());
        Long managerId = null;
        String managerName = null;
        var mapping = userManagerMappingRepository.findByUserId(id);
        if (mapping.isPresent()) {
            managerId = mapping.get().getManager().getId();
            managerName = mapping.get().getManager().getFullName();
        }

        return new UserDetailResponse(
                user.getId(),
                user.getFullName(),
                user.getDepartment(),
                user.getDesignation(),
                user.getStatus(),
                totalPct,
                managerId,       // ← new
                managerName,     // ← new
                skills,
                activeAllocations,
                recentTags
        );

    }

    // ── Create user ────────────────────────────────────────────────────────

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException(
                        "Role not found: " + request.getRole()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getTemporaryPassword()));
        user.setRole(role);
        user.setForcePasswordChange(true);
        user.setActive(true);
        user.setFullName(request.getFullName());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getDesignation() != null) user.setDesignation(request.getDesignation());

        return mapToResponse(userRepository.save(user));
    }

    // ── Update profile fields ──────────────────────────────────────────────

    public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank())
            user.setFullName(request.getFullName());
        if (request.getEmail() != null && !request.getEmail().isBlank())
            user.setEmail(request.getEmail());
        if (request.getDepartment() != null && !request.getDepartment().isBlank())
            user.setDepartment(request.getDepartment());
        if (request.getDesignation() != null && !request.getDesignation().isBlank())
            user.setDesignation(request.getDesignation());
        if (user.getStatus() == null) {
            user.setStatus("BENCH");
        }
        return mapToResponse(userRepository.save(user));
    }

    // ── Password / activation ──────────────────────────────────────────────

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
        if (!user.isActive())
            throw new RuntimeException("User is already deactivated");
        user.setActive(false);
        userRepository.save(user);
    }

    public void reactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isActive())
            throw new RuntimeException("User is already active");
        user.setActive(true);
        userRepository.save(user);
    }

    // ── Profile update (department + designation only) ─────────────────────

    public UserResponse updateProfile(Long userId,
                                      String department, String designation) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (department != null && !department.isBlank())
            user.setDepartment(department);
        if (designation != null && !designation.isBlank())
            user.setDesignation(designation);
        if (user.getStatus() == null) {
            user.setStatus("BENCH");
        }
        return mapToResponse(userRepository.save(user));
    }

    // ── Skills ─────────────────────────────────────────────────────────────

    public List<SkillResponse> getUserSkills(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return employeeSkillRepository.findByUserId(userId)
                .stream()
                .map(es -> new SkillResponse(
                        es.getSkill().getId(),
                        es.getSkill().getName(),
                        es.getSkill().getCategory(),
                        es.getProficiency()))
                .collect(Collectors.toList());
    }

    public void addSkill(Long userId, SkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        EmployeeSkillId esId = new EmployeeSkillId(userId, skill.getId());
        if (employeeSkillRepository.existsById(esId))
            throw new RuntimeException("Skill already assigned to this user");

        EmployeeSkill es = new EmployeeSkill();
        es.setId(esId);
        es.setUser(user);
        es.setSkill(skill);
        es.setProficiency(request.getProficiency());
        employeeSkillRepository.save(es);
    }

    public void updateSkillProficiency(Long userId, Long skillId,
                                       SkillRequest request) {
        EmployeeSkillId esId = new EmployeeSkillId(userId, skillId);
        EmployeeSkill es = employeeSkillRepository.findById(esId)
                .orElseThrow(() ->
                        new RuntimeException("Skill not assigned to this user"));
        es.setProficiency(request.getProficiency());
        employeeSkillRepository.save(es);
    }

    public void removeSkill(Long userId, Long skillId) {
        EmployeeSkillId esId = new EmployeeSkillId(userId, skillId);
        if (!employeeSkillRepository.existsById(esId))
            throw new RuntimeException("Skill not assigned to this user");
        employeeSkillRepository.deleteById(esId);
    }

    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(),
                        s.getCategory(), null))
                .collect(Collectors.toList());
    }

    public SkillResponse createSkill(CreateSkillRequest request) {
        if (skillRepository.existsByName(request.getName()))
            throw new RuntimeException("Skill already exists");
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        Skill saved = skillRepository.save(skill);
        return new SkillResponse(saved.getId(), saved.getName(),
                saved.getCategory(), null);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private UserResponse mapToResponse(User user) {
        List<SkillResponse> skills = employeeSkillRepository
                .findByUserId(user.getId())
                .stream()
                .map(es -> new SkillResponse(
                        es.getSkill().getId(),
                        es.getSkill().getName(),
                        es.getSkill().getCategory(),
                        es.getProficiency()))
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName(),
                user.isActive(),
                user.isForcePasswordChange(),
                user.getFullName(),
                user.getDepartment(),
                user.getDesignation(),
                user.getStatus(),
                skills
        );
    }
}

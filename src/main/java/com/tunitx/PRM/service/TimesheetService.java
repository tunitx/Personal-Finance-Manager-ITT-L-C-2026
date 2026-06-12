package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.timesheet.*;
import com.tunitx.PRM.model.ActivityTag;
import com.tunitx.PRM.model.Timesheet;
import com.tunitx.PRM.model.TimesheetEntry;
import com.tunitx.PRM.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final ActivityTagRepository activityTagRepository;
    private final AllocationRepository allocationRepository;
    private final UserRepository userRepository;               // ← was EmployeeRepository

    public List<TagResponse> getAllTags() {
        return activityTagRepository.findAll()
                .stream()
                .map(t -> new TagResponse(t.getId(), t.getName()))
                .collect(Collectors.toList());
    }

    public List<ActiveAllocationResponse> getActiveAllocations(
            Long userId, LocalDate weekStart) {               // ← param renamed

        return allocationRepository
                .findByUserIdAndIsActive(userId, true)         // ← was findByEmployeeIdAndIsActive
                .stream()
                .filter(a -> !a.getFromDate().isAfter(weekStart)
                        && !a.getToDate().isBefore(weekStart))
                .map(a -> new ActiveAllocationResponse(
                        a.getProject().getId(),
                        a.getProject().getName(),
                        a.getUtilisationPct(),
                        a.getFromDate(),
                        a.getToDate()
                ))
                .collect(Collectors.toList());
    }

    public List<TimesheetResponse> getMyTimesheets(Long userId) {   // ← param renamed
        return timesheetRepository.findByUserId(userId)              // ← was findByEmployeeId
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TimesheetResponse> getTeamTimesheets(
            Long managerId, LocalDate weekStart) {

        List<Long> teamUserIds = allocationRepository
                .findByProjectId(managerId)
                .stream()
                .map(a -> a.getUser().getId())                       // ← was getEmployee()
                .distinct()
                .collect(Collectors.toList());

        List<Timesheet> timesheets;
        if (weekStart != null) {
            timesheets = timesheetRepository
                    .findByUserIdInAndWeekStart(teamUserIds, weekStart);    // ← was findByEmployeeIdInAndWeekStart
        } else {
            timesheets = timesheetRepository
                    .findByUserIdIn(teamUserIds);                           // ← was findByEmployeeIdIn
        }

        return timesheets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TimesheetResponse submitTimesheet(
            Long userId, SubmitTimesheetRequest request) {             // ← param renamed

        LocalDate weekStart = request.getWeekStart();

        if (weekStart.isAfter(LocalDate.now())) {
            throw new RuntimeException("Cannot submit timesheet for a future week");
        }

        if (timesheetRepository.findByUserIdAndWeekStart(             // ← was findByEmployeeIdAndWeekStart
                userId, weekStart).isPresent()) {
            throw new RuntimeException("Timesheet already submitted for this week");
        }

        List<ActiveAllocationResponse> activeAllocations =
                getActiveAllocations(userId, weekStart);

        List<Long> activeProjectIds = activeAllocations.stream()
                .map(ActiveAllocationResponse::getProjectId)
                .collect(Collectors.toList());

        int totalHours = request.getEntries().stream()
                .mapToInt(TimesheetEntryRequest::getHoursLogged)
                .sum();

        if (totalHours > 40) {
            throw new RuntimeException(
                    "Total hours (" + totalHours
                            + ") exceed maximum weekly hours (40)");
        }

        for (TimesheetEntryRequest entry : request.getEntries()) {
            if (!activeProjectIds.contains(entry.getProjectId())) {
                throw new RuntimeException(
                        "Project ID " + entry.getProjectId()
                                + " is not in your active allocations for this week");
            }
        }

        Timesheet timesheet = new Timesheet();
        timesheet.setUser(                                             // ← was setEmployee
                userRepository.findById(userId)                        // ← was employeeRepository
                        .orElseThrow(() -> new RuntimeException("User not found")));
        timesheet.setWeekStart(weekStart);
        timesheet.setStatus("SUBMITTED");
        timesheet.setSubmittedAt(LocalDateTime.now());

        Timesheet saved = timesheetRepository.save(timesheet);

        for (TimesheetEntryRequest entryRequest : request.getEntries()) {
            TimesheetEntry entry = new TimesheetEntry();
            entry.setTimesheet(saved);
            entry.setProject(allocationRepository
                    .findByUserIdAndIsActive(userId, true)             // ← was findByEmployeeIdAndIsActive
                    .stream()
                    .filter(a -> a.getProject().getId()
                            .equals(entryRequest.getProjectId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Project not found"))
                    .getProject());
            entry.setHoursLogged(entryRequest.getHoursLogged());

            if (entryRequest.getTagIds() != null
                    && !entryRequest.getTagIds().isEmpty()) {
                List<ActivityTag> tags = activityTagRepository
                        .findAllByIdIn(entryRequest.getTagIds());
                entry.setTags(tags);
            }

            timesheetEntryRepository.save(entry);
        }

        return mapToResponse(saved);
    }

    private TimesheetResponse mapToResponse(Timesheet timesheet) {
        List<TimesheetEntryResponse> entries = timesheetEntryRepository
                .findByTimesheetId(timesheet.getId())
                .stream()
                .map(e -> new TimesheetEntryResponse(
                        e.getId(),
                        e.getProject().getId(),
                        e.getProject().getName(),
                        e.getHoursLogged(),
                        e.getTags() == null ? List.of() :
                                e.getTags().stream()
                                        .map(t -> new TagResponse(
                                                t.getId(), t.getName()))
                                        .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        int totalHours = entries.stream()
                .mapToInt(TimesheetEntryResponse::getHoursLogged)
                .sum();

        return new TimesheetResponse(
                timesheet.getId(),
                timesheet.getUser().getId(),                           // ← was getEmployee()
                timesheet.getUser().getFullName(),                     // ← was getEmployee()
                timesheet.getWeekStart(),
                timesheet.getStatus(),
                totalHours,
                timesheet.getSubmittedAt(),
                entries
        );
    }
}
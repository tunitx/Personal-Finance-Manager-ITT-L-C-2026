package com.tunitx.PRM.service;

import com.tunitx.PRM.ai.LlmProvider;
import com.tunitx.PRM.ai.LlmProviderFactory;
import com.tunitx.PRM.dto.ai.AiResponse;
import com.tunitx.PRM.dto.ai.RiskSummaryRequest;
import com.tunitx.PRM.dto.ai.SkillMatchRequest;
import com.tunitx.PRM.model.*;
import com.tunitx.PRM.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final LlmProviderFactory llmProviderFactory;
    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;
    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserSkillRepository userSkillRepository;

    // ── Skill Match ────────────────────────────────────────────────────────

    public AiResponse skillMatch(SkillMatchRequest request) {

        // Fetch config — key comes from DB, not system properties
        SystemConfig config = systemConfigRepository.getConfig();
        LlmProvider provider = llmProviderFactory
                .getProvider(config.getLlmProvider());
        String apiKey = config.getLlmApiKeyEnc() != null
                ? config.getLlmApiKeyEnc() : "";

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Bench users only — exclude ADMIN
        List<User> benchUsers = userRepository.findByStatus("BENCH")
                .stream()
                .filter(u -> !u.getRole().getName().equals("ADMIN"))
                .collect(Collectors.toList());

        StringBuilder userData = new StringBuilder();
        for (User user : benchUsers) {

            // Profile skills
            String profileSkills = userSkillRepository
                    .findByUserId(user.getId())
                    .stream()
                    .map(es -> es.getSkill().getName()
                            + " (" + es.getProficiency() + ")")
                    .collect(Collectors.joining(", "));

            // Recent activity tags from timesheets
            String recentActivity = timesheetRepository
                    .findByUserId(user.getId())
                    .stream()
                    .flatMap(t -> timesheetEntryRepository
                            .findByTimesheetId(t.getId()).stream())
                    .flatMap(e -> e.getTags() == null
                            ? java.util.stream.Stream.empty()
                            : e.getTags().stream())
                    .map(ActivityTag::getName)
                    .distinct()
                    .collect(Collectors.joining(", "));

            userData.append("- ").append(user.getFullName())
                    .append(" (").append(user.getDesignation() != null
                            ? user.getDesignation() : "—").append(")")
                    .append(" | Department: ").append(user.getDepartment() != null
                            ? user.getDepartment() : "—")
                    .append(" | Skills: ").append(
                            profileSkills.isEmpty() ? "None" : profileSkills)
                    .append(" | Recent activity: ").append(
                            recentActivity.isEmpty() ? "None" : recentActivity)
                    .append("\n");
        }

        String prompt = """
                You are a resource allocation assistant for a software company.
                
                Project: %s
                Manager requirement: %s
                
                Available employees (currently on bench):
                %s
                
                Based on the requirement and employee profiles and recent activity,
                suggest the top 2-3 best matches. For each match provide:
                - Their name
                - Why they are suitable (skills and recent work evidence)
                Be concise and professional. If no one matches well, say so clearly.
                """.formatted(
                project.getName(),
                request.getRequirement(),
                userData.toString().isEmpty()
                        ? "No employees currently on bench."
                        : userData.toString()
        );

        // Pass apiKey directly — no System.setProperty
        String result = provider.generateText(prompt, apiKey);

        return new AiResponse(result,
                "AI-generated suggestions. Verify before confirming allocation.");
    }

    // ── Risk Summary ───────────────────────────────────────────────────────

    public AiResponse riskSummary(RiskSummaryRequest request) {

        // Fetch config — key comes from DB, not system properties
        SystemConfig config = systemConfigRepository.getConfig();
        LlmProvider provider = llmProviderFactory
                .getProvider(config.getLlmProvider());
        String apiKey = config.getLlmApiKeyEnc() != null
                ? config.getLlmApiKeyEnc() : "";

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Milestones
        List<Milestone> milestones = milestoneRepository
                .findByProjectId(project.getId());

        StringBuilder milestoneData = new StringBuilder();
        for (Milestone m : milestones) {
            milestoneData.append("- ").append(m.getTitle())
                    .append(" | Due: ").append(m.getDueDate())
                    .append(" | Status: ").append(m.getStatus())
                    .append("\n");
        }

        // Active allocations + last week hours per person
        List<Allocation> allocations = allocationRepository
                .findByProjectId(project.getId());

        StringBuilder timesheetData = new StringBuilder();
        for (Allocation allocation : allocations) {

            User user = allocation.getUser();

            int expectedHours = (allocation.getUtilisationPct()
                    * config.getMaxWeeklyHours()) / 100;

            int totalHoursLastWeek = timesheetRepository
                    .findByUserId(user.getId())
                    .stream()
                    .sorted((a, b) -> b.getWeekStart().compareTo(a.getWeekStart()))
                    .limit(1)
                    .flatMap(t -> timesheetEntryRepository
                            .findByTimesheetId(t.getId()).stream())
                    .filter(e -> e.getProject().getId().equals(project.getId()))
                    .mapToInt(TimesheetEntry::getHoursLogged)
                    .sum();

            timesheetData.append("- ").append(user.getFullName())
                    .append(" | Utilisation: ")
                    .append(allocation.getUtilisationPct()).append("%")
                    .append(" | Expected hrs/week: ").append(expectedHours)
                    .append(" | Actual hrs last week: ").append(totalHoursLastWeek)
                    .append("\n");
        }

        String prompt = """
                You are a project risk analyst for a software company.
                
                Project: %s
                Status: %s
                Timeline: %s to %s
                
                Milestones:
                %s
                
                Team timesheet data:
                %s
                
                Based on the above data, provide a concise risk summary for this
                project. Highlight any concerns about delays, overdue milestones,
                or low team activity. Keep it under 150 words and be direct.
                """.formatted(
                project.getName(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                milestoneData.toString().isEmpty()
                        ? "No milestones defined." : milestoneData.toString(),
                timesheetData.toString().isEmpty()
                        ? "No timesheet data available."
                        : timesheetData.toString()
        );

        // Pass apiKey directly — no System.setProperty
        String result = provider.generateText(prompt, apiKey);

        return new AiResponse(result,
                "AI-generated from milestone and timesheet data.");
    }
}
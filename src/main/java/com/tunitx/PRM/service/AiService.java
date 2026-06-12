package com.tunitx.PRM.service;

import com.tunitx.PRM.ai.LlmProvider;
import com.tunitx.PRM.ai.LlmProviderFactory;
import com.tunitx.PRM.dto.ai.*;
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

    public TeamBuildResponse teamBuilder(TeamBuildRequest request) {

        SystemConfig config = systemConfigRepository.getConfig();
        LlmProvider provider = llmProviderFactory
                .getProvider(config.getLlmProvider());
        String apiKey = config.getLlmApiKeyEnc() != null
                ? config.getLlmApiKeyEnc() : "";

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // All bench users excluding ADMIN
        List<User> benchUsers = userRepository.findByStatus("BENCH")
                .stream()
                .filter(u -> !u.getRole().getName().equals("ADMIN"))
                .collect(Collectors.toList());

        // Build employee data block — id included so AI can reference them
        StringBuilder employeeData = new StringBuilder();
        for (User user : benchUsers) {
            String skills = userSkillRepository
                    .findByUserId(user.getId())
                    .stream()
                    .map(es -> es.getSkill().getName()
                            + " (" + es.getProficiency() + ")")
                    .collect(Collectors.joining(", "));

            employeeData
                    .append("ID:").append(user.getId())
                    .append(" | ").append(user.getFullName())
                    .append(" (").append(user.getDesignation() != null
                            ? user.getDesignation() : "—").append(")")
                    .append(" | Dept: ").append(user.getDepartment() != null
                            ? user.getDepartment() : "—")
                    .append(" | Skills: ").append(
                            skills.isEmpty() ? "None" : skills)
                    .append("\n");
        }

        // Build roles block
        StringBuilder rolesData = new StringBuilder();
        for (int i = 0; i < request.getRoles().size(); i++) {
            TeamBuildRequest.RoleRequirement role = request.getRoles().get(i);
            rolesData.append(i + 1).append(". ").append(role.getRoleName())
                    .append(" | Required skills: ")
                    .append(String.join(", ", role.getRequiredSkills()))
                    .append(" | Min proficiency: ").append(
                            role.getMinProficiency() != null
                                    ? role.getMinProficiency() : "Any")
                    .append("\n");
        }

        System.out.println(rolesData.toString());

        String prompt = """
                You are a resource allocation assistant for a software company.
                Fill every role below with the best available bench employee.
                RULES:
                - Never assign the same employee to more than one role.
                - If no suitable employee exists for a role, say UNFILLED and explain why
                  (either: "No employee has this skill" or "Employee with this skill is
                  already assigned to role X").
                - Be concise and direct.
                
                Project: %s
                
                Roles needed:
                %s
                
                Available bench employees:
                %s
                
                Respond ONLY in this exact JSON format, no extra text:
                {
                  "assignments": [
                    {
                      "roleName": "...",
                      "filled": true,
                      "assignedUserId": "123",
                      "assignedUserName": "...",
                      "reason": "..."
                    },
                    {
                      "roleName": "...",
                      "filled": false,
                      "assignedUserId": null,
                      "assignedUserName": null,
                      "reason": null,
                      "gapReason": "No employee has Java Advanced skill"
                    }
                  ]
                }
                """.formatted(
                project.getName(),
                rolesData.toString(),
                employeeData.toString().isEmpty()
                        ? "No employees currently on bench." : employeeData.toString()
        );

        String rawResult = provider.generateText(prompt, apiKey);

        // Parse AI JSON response
        List<TeamBuildResponse.RoleAssignment> assignments = parseAssignments(
                rawResult, request.getRoles());

        return new TeamBuildResponse(
                project.getName(),
                assignments,
                "AI-generated team. Review before confirming allocations."
        );
    }

    private List<TeamBuildResponse.RoleAssignment> parseAssignments(
            String rawResult,
            List<TeamBuildRequest.RoleRequirement> roles) {

        List<TeamBuildResponse.RoleAssignment> assignments = new java.util.ArrayList<>();
        try {
            // Strip markdown code fences if present
            String json = rawResult
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode arr = root.get("assignments");

            if (arr != null && arr.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                    assignments.add(new TeamBuildResponse.RoleAssignment(
                            node.path("roleName").asText("—"),
                            node.path("assignedUserId").isNull()
                                    ? null : node.path("assignedUserId").asText(),
                            node.path("assignedUserName").isNull()
                                    ? null : node.path("assignedUserName").asText(),
                            node.path("reason").isNull()
                                    ? null : node.path("reason").asText(),
                            node.path("filled").asBoolean(false),
                            node.path("gapReason").isNull()
                                    ? null : node.path("gapReason").asText()
                    ));
                }
            }
        } catch (Exception e) {
            // Fallback — AI didn't return valid JSON
            // Create one UNFILLED assignment per role with the raw result as gap reason
            for (TeamBuildRequest.RoleRequirement role : roles) {
                assignments.add(new TeamBuildResponse.RoleAssignment(
                        role.getRoleName(),
                        null, null, null, false,
                        "AI response could not be parsed. Raw: " + rawResult
                ));
            }
        }
        return assignments;
    }
}
package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.ai.AiResponse;
import com.tunitx.PRM.dto.ai.RiskSummaryRequest;
import com.tunitx.PRM.dto.ai.SkillMatchRequest;
import com.tunitx.PRM.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class AiController {

    private final AiService aiService;

    @PostMapping("/skill-match")
    public ResponseEntity<AiResponse> skillMatch(
            @Valid @RequestBody SkillMatchRequest request) {
        return ResponseEntity.ok(aiService.skillMatch(request));
    }

    @PostMapping("/risk-summary")
    public ResponseEntity<AiResponse> riskSummary(
            @Valid @RequestBody RiskSummaryRequest request) {
        return ResponseEntity.ok(aiService.riskSummary(request));
    }
}
package com.tunitx.PRM.controller;

import com.tunitx.PRM.dto.config.ConfigResponse;
import com.tunitx.PRM.dto.config.UpdateConfigRequest;
import com.tunitx.PRM.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<ConfigResponse> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }

    @PutMapping
    public ResponseEntity<ConfigResponse> updateConfig(
            @Valid @RequestBody UpdateConfigRequest request) {
        return ResponseEntity.ok(configService.updateConfig(request));
    }
}
package com.tunitx.PRM.service;

import com.tunitx.PRM.dto.config.ConfigResponse;
import com.tunitx.PRM.dto.config.UpdateConfigRequest;
import com.tunitx.PRM.model.SystemConfig;
import com.tunitx.PRM.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public ConfigResponse getConfig() {
        SystemConfig config = systemConfigRepository.getConfig();
        return new ConfigResponse(
                config.getLlmProvider(),
                maskApiKey(config.getLlmApiKeyEnc()),
                config.getSchedulerIntervalHrs(),
                config.getMaxWeeklyHours()
        );
    }

    public ConfigResponse updateConfig(UpdateConfigRequest request) {
        SystemConfig config = systemConfigRepository.getConfig();

        if (request.getLlmProvider() != null) {
            config.setLlmProvider(request.getLlmProvider());
        }
        if (request.getLlmApiKey() != null) {
            config.setLlmApiKeyEnc(request.getLlmApiKey());
        }
        if (request.getSchedulerIntervalHrs() != null) {
            config.setSchedulerIntervalHrs(request.getSchedulerIntervalHrs());
        }
        if (request.getMaxWeeklyHours() != null) {
            config.setMaxWeeklyHours(request.getMaxWeeklyHours());
        }

        SystemConfig saved = systemConfigRepository.save(config);
        return new ConfigResponse(
                saved.getLlmProvider(),
                maskApiKey(saved.getLlmApiKeyEnc()),
                saved.getSchedulerIntervalHrs(),
                saved.getMaxWeeklyHours()
        );
    }

    private String maskApiKey(String key) {
        if (key == null || key.isEmpty()) return "Not set";
        return "*".repeat(Math.min(key.length(), 20)) + "...";
    }
}
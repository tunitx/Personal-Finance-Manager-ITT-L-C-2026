package com.tunitx.PRM.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GeminiAdapter implements LlmProvider {

    @Override
    public String generateText(String prompt, String apiKey) {
        return "";
    }
}
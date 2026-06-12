package com.tunitx.PRM.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmProviderFactory {

    private final GeminiAdapter geminiAdapter;
    private final GroqAdapter groqAdapter;
    private final GemmaAdaptor gemmaAdaptor;

    public LlmProvider getProvider(String providerName) {
        return switch (providerName.toUpperCase()) {
            case "GEMINI" -> geminiAdapter;
            case "GROQ" -> groqAdapter;
            case "GEMMA" -> gemmaAdaptor;
            default -> throw new RuntimeException(
                    "Unknown LLM provider: " + providerName);
        };
    }
}
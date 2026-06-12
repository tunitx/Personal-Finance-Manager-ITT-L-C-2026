package com.tunitx.PRM.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GroqAdapter implements LlmProvider {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateText(String prompt, String apiKey) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Groq raw response: {}", response);

            JsonNode root = objectMapper.readTree(response);
            return root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("No response generated.");

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            return "AI service unavailable: " + e.getMessage();
        }
    }
}
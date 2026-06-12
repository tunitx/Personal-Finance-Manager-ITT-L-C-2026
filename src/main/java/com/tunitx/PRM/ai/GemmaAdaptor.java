package com.tunitx.PRM.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunitx.PRM.ai.LlmProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class GemmaAdaptor implements LlmProvider {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateText(String prompt, String apiKey) {
        try {
            // Building the request body to match the Gemma API payload
            Map<String, Object> requestBody = Map.of(
                    "model", "gemma3:12b-it-q8_0",
                    "prompt", prompt,
                    "stream", false
            );

            String response = restClient.post()
                    .uri("http://164.52.211.238/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    // Note: Using exact header key "apikey" as shown in the curl command
                    .header("apikey", apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Gemma raw response: {}", response);

            JsonNode root = objectMapper.readTree(response);

            // Extracting the text directly from the root "response" field
            return root.path("response").asText("No response generated.");

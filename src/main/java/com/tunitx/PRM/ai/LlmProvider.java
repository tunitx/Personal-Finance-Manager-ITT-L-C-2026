package com.tunitx.PRM.ai;

public interface LlmProvider {

    /**
     * Generate text from the LLM.
     *
     * @param prompt the full prompt to send
     * @param apiKey the API key fetched from system_config by AiService
     */
    String generateText(String prompt, String apiKey);
}
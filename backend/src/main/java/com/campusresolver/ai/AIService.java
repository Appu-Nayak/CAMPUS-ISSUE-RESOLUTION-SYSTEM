package com.campusresolver.ai;

import com.campusresolver.dto.AIClassificationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * AI Classification Service
 *
 * This service sends a campus complaint to the OpenRouter API and
 * parses the returned JSON to extract:
 *   - category  (wifi / hostel / transport / maintenance / other)
 *   - priority  (urgent / normal)
 *   - reasoning (AI's explanation)
 *   - suggestedDepartment
 *
 * How it works:
 *  1. Build a structured prompt asking the LLM to classify
 *  2. Call OpenRouter API (which routes to GPT-3.5 / Mistral / etc.)
 *  3. Parse the JSON response from the LLM
 *  4. Return AIClassificationResult
 *
 * If the API is unavailable or returns an error, a fallback
 * classification is returned so the system keeps working.
 */
@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.api.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    /**
     * Main method: classifies a complaint using AI.
     *
     * @param complaintTitle       The complaint title
     * @param complaintDescription The full complaint description
     * @return AIClassificationResult with category, priority, and department suggestion
     */
    public AIClassificationResult classifyComplaint(String complaintTitle, String complaintDescription) {
        logger.info("Starting AI classification for complaint: '{}'", complaintTitle);

        try {
            // 1. Build the prompt
            String prompt = buildClassificationPrompt(complaintTitle, complaintDescription);

            // 2. Build the request body JSON
            String requestBody = buildRequestBody(prompt);

            // 3. Call the OpenRouter API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://campus-resolver.app")  // Required by OpenRouter
                    .header("X-Title", "Campus Issue Resolver")              // Optional: app name
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("OpenRouter API response status: {}", response.statusCode());
            logger.debug("OpenRouter API response body: {}", response.body());

            // 4. Check for errors
            if (response.statusCode() != 200) {
                logger.error("OpenRouter API error: HTTP {} - {}", response.statusCode(), response.body());
                return getFallbackClassification();
            }

            // 5. Parse the LLM's response
            return parseAIResponse(response.body());

        } catch (Exception e) {
            logger.error("Error calling OpenRouter API: {}", e.getMessage(), e);
            return getFallbackClassification();
        }
    }

    /**
     * Builds a clear, structured prompt for the AI.
     * The prompt explicitly requests JSON output.
     */
    private String buildClassificationPrompt(String title, String description) {
        return String.format("""
            You are a campus complaint classifier. Analyze the following campus complaint and classify it.
            
            Complaint Title: %s
            Complaint Description: %s
            
            Classify this complaint and respond ONLY with a valid JSON object. No explanation outside JSON.
            
            Use these exact categories: wifi, hostel, transport, maintenance, other
            Use these exact priorities: urgent, normal
            
            Rules for priority:
            - urgent: if it affects safety, exams, health, or many students
            - normal: routine issues
            
            Respond ONLY with this JSON format:
            {
              "category": "one of: wifi/hostel/transport/maintenance/other",
              "priority": "urgent or normal",
              "reasoning": "brief explanation in one sentence",
              "suggestedDepartment": "name of the department to handle this"
            }
            """, title, description);
    }

    /**
     * Builds the JSON request body for the OpenRouter API.
     * Uses the standard OpenAI chat completion format.
     */
    private String buildRequestBody(String prompt) throws Exception {
        // Build using Jackson ObjectMapper to ensure proper JSON escaping
        var bodyMap = new java.util.HashMap<String, Object>();
        bodyMap.put("model", model);
        bodyMap.put("max_tokens", 300);

        var messageMap = new java.util.HashMap<String, String>();
        messageMap.put("role", "user");
        messageMap.put("content", prompt);

        bodyMap.put("messages", new Object[]{messageMap});

        return objectMapper.writeValueAsString(bodyMap);
    }

    /**
     * Parses the raw API response JSON and extracts the classification result.
     *
     * OpenRouter response structure:
     * {
     *   "choices": [{
     *     "message": {
     *       "content": "{ ...classification JSON... }"
     *     }
     *   }]
     * }
     */
    private AIClassificationResult parseAIResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // Extract the LLM's text content from the response
        String content = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        logger.debug("AI response content: {}", content);

        // The content should itself be a JSON string — parse it
        // Clean up in case the LLM adds markdown code blocks
        content = content.trim();
        if (content.startsWith("```")) {
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();
        }

        JsonNode aiJson = objectMapper.readTree(content);

        String category  = aiJson.path("category").asText("other").toLowerCase();
        String priority  = aiJson.path("priority").asText("normal").toLowerCase();
        String reasoning = aiJson.path("reasoning").asText("Classified by AI");
        String dept      = aiJson.path("suggestedDepartment").asText("Admin Office");

        // Validate and sanitize category
        if (!isValidCategory(category)) {
            logger.warn("AI returned unknown category '{}', defaulting to 'other'", category);
            category = "other";
        }

        // Validate and sanitize priority
        if (!priority.equals("urgent") && !priority.equals("normal")) {
            logger.warn("AI returned unknown priority '{}', defaulting to 'normal'", priority);
            priority = "normal";
        }

        logger.info("AI Classification: category={}, priority={}", category, priority);

        return AIClassificationResult.builder()
                .category(category)
                .priority(priority)
                .reasoning(reasoning)
                .suggestedDepartment(dept)
                .build();
    }

    /**
     * Returns a safe default classification when AI is unavailable.
     * Ensures the system never breaks even if OpenRouter is down.
     */
    private AIClassificationResult getFallbackClassification() {
        logger.warn("Using fallback classification (AI service unavailable)");
        return AIClassificationResult.builder()
                .category("other")
                .priority("normal")
                .reasoning("Auto-classified as general complaint (AI service temporarily unavailable)")
                .suggestedDepartment("Admin Office")
                .build();
    }

    private boolean isValidCategory(String category) {
        return category != null &&
               (category.equals("wifi") || category.equals("hostel") ||
                category.equals("transport") || category.equals("maintenance") ||
                category.equals("other"));
    }
}

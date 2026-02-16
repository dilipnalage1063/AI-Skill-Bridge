package com.dilip.studyplan.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * AI client that optionally calls OpenRouter (OpenAI-compatible API).
 *
 * - If a valid OpenRouter key+model are configured, it will use a real LLM.
 * - If anything fails (no key, bad model, network, quota), it silently falls
 *   back to a local rule-based plan, so the app never shows AI errors.
 */
@Component
public class AIClient {

    private static final String OPENROUTER_CHAT_COMPLETIONS_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;

    public AIClient(
            @Value("${openrouter.api.key:}") String apiKey,
            @Value("${openrouter.model:}") String model
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null ? "" : model.trim();

        // Configure timeouts so calls don't hang forever
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);

        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Public entry point used by the service.
     * Always returns a valid plan string, never an error message.
     */
    public String generateStudyPlan(String topic, int days) {

        // 1) Try real AI via OpenRouter, if configured
        String aiPlan = tryOpenRouterStudyPlan(topic, days);
        if (aiPlan != null && !aiPlan.isBlank()) {
            return aiPlan;
        }

        // 2) Guaranteed offline fallback (no external dependencies)
        return buildOfflinePlan(topic, days);
    }

    /**
     * Attempts to call OpenRouter. Returns null on any failure so callers
     * can safely fall back to local generation.
     */
    private String tryOpenRouterStudyPlan(String topic, int days) {
        // If key or model are not configured, skip remote entirely
        if (apiKey.isEmpty() || model.isEmpty()) {
            return null;
        }

        if (days <= 0) {
            days = 5;
        }

        String normalizedTopic = (topic == null || topic.isBlank())
                ? "your chosen topic"
                : topic.trim();

        String systemMessage =
                "You are an expert study planner. " +
                "You create clear, concise, practical, day-wise study plans " +
                "with friendly emojis where helpful (for example 📘, ✅, 💡, 🔁), " +
                "but you NEVER include code fences or markdown headings.";

        String userMessage =
                "Create a strictly structured " + days + "-day study plan for learning " + normalizedTopic + ".\n\n" +
                "Rules:\n" +
                "1. Break strictly day-wise.\n" +
                "2. Use headings like 'Day 1:', 'Day 2:'.\n" +
                "3. Add bullet points under each day.\n" +
                "4. Be concise and practical.\n" +
                "5. Use simple emojis and symbols to make it engaging (for example 📘, ✅, 💡).\n" +
                "6. No markdown, no tables, just plain text with bullets.\n";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // OpenRouter uses Bearer token, OpenAI-style
        headers.setBearerAuth(apiKey);
        // Optional but recommended metadata
        headers.set("X-Title", "AI Study Planner Demo");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OPENROUTER_CHAT_COMPLETIONS_URL, entity, Map.class);

            Object bodyObj = response.getBody();
            if (!(bodyObj instanceof Map<?, ?> body)) {
                return null;
            }

            Object choicesObj = body.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return null;
            }

            Object firstChoiceObj = choices.get(0);
            if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
                return null;
            }

            Object messageObj = firstChoice.get("message");
            if (!(messageObj instanceof Map<?, ?> message)) {
                return null;
            }

            Object contentObj = message.get("content");
            if (contentObj == null) {
                return null;
            }

            return contentObj.toString();

        } catch (Exception ignored) {
            // Any error: just fall back to offline plan
            return null;
        }
    }

    /**
     * Analyzes requirements in detail (extracts skills, levels, critical skills, etc.)
     */
    public String analyzeRequirements(String prompt) {
        if (apiKey.isEmpty() || model.isEmpty()) {
            return null;
        }

        String systemMessage = "You are an expert requirements analyst. Analyze job descriptions, exam syllabi, or goal descriptions and extract detailed requirements including skills, skill levels, critical vs nice-to-have skills, difficulty assessment, and timeline recommendations. Always respond in the exact format requested.";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("X-Title", "AI Study Planner - Requirements Analysis");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OPENROUTER_CHAT_COMPLETIONS_URL, entity, Map.class);

            Object bodyObj = response.getBody();
            if (!(bodyObj instanceof Map<?, ?> body)) {
                return null;
            }

            Object choicesObj = body.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return null;
            }

            Object firstChoiceObj = choices.get(0);
            if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
                return null;
            }

            Object messageObj = firstChoice.get("message");
            if (!(messageObj instanceof Map<?, ?> message)) {
                return null;
            }

            Object contentObj = message.get("content");
            if (contentObj == null) {
                return null;
            }

            return contentObj.toString().trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Extracts skills from text (job description, exam syllabus, etc.)
     */
    public String extractSkills(String prompt) {
        if (apiKey.isEmpty() || model.isEmpty()) {
            return null;
        }

        String systemMessage = "You are a skill extraction assistant. Extract technical skills and technologies from text. Return ONLY a comma-separated list of skill names, no explanations.";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("X-Title", "AI Study Planner - Skill Extraction");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OPENROUTER_CHAT_COMPLETIONS_URL, entity, Map.class);

            Object bodyObj = response.getBody();
            if (!(bodyObj instanceof Map<?, ?> body)) {
                return null;
            }

            Object choicesObj = body.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return null;
            }

            Object firstChoiceObj = choices.get(0);
            if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
                return null;
            }

            Object messageObj = firstChoice.get("message");
            if (!(messageObj instanceof Map<?, ?> message)) {
                return null;
            }

            Object contentObj = message.get("content");
            if (contentObj == null) {
                return null;
            }

            return contentObj.toString().trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Generates a gap-aware study plan that prioritizes missing skills.
     */
    public String generateGapAwarePlan(String topic, int days, List<com.dilip.studyplan.dto.SkillGap> gaps, String goalType, List<String> prioritySkills) {
        if (apiKey.isEmpty() || model.isEmpty()) {
            return buildGapAwareOfflinePlan(topic, days, gaps, prioritySkills);
        }

        // Build priority context based on available days
        StringBuilder gapContext = new StringBuilder();
        gapContext.append("Priority skills to focus on (based on ").append(days).append(" days available):\n");
        
        // Group gaps by priority
        List<com.dilip.studyplan.dto.SkillGap> highPriority = gaps.stream()
                .filter(g -> "HIGH".equals(g.getPriority()) && prioritySkills.contains(g.getSkillName()))
                .sorted((a, b) -> Integer.compare(b.getGapSize(), a.getGapSize()))
                .collect(java.util.stream.Collectors.toList());
        
        List<com.dilip.studyplan.dto.SkillGap> mediumPriority = gaps.stream()
                .filter(g -> "MEDIUM".equals(g.getPriority()) && prioritySkills.contains(g.getSkillName()))
                .sorted((a, b) -> Integer.compare(b.getGapSize(), a.getGapSize()))
                .collect(java.util.stream.Collectors.toList());

        if (!highPriority.isEmpty()) {
            gapContext.append("\nHIGH PRIORITY (Focus in first ").append(Math.min(days / 2, days)).append(" days):\n");
            for (com.dilip.studyplan.dto.SkillGap gap : highPriority) {
                gapContext.append("- ").append(gap.getSkillName())
                          .append(" (gap: ").append(gap.getGapSize()).append(" levels, current: ").append(gap.getCurrentLevel()).append("/5)\n");
            }
        }
        
        if (!mediumPriority.isEmpty() && days > 7) {
            gapContext.append("\nMEDIUM PRIORITY (Focus in remaining days):\n");
            for (com.dilip.studyplan.dto.SkillGap gap : mediumPriority) {
                gapContext.append("- ").append(gap.getSkillName())
                          .append(" (gap: ").append(gap.getGapSize()).append(" levels, current: ").append(gap.getCurrentLevel()).append("/5)\n");
            }
        }

        String systemMessage =
                "You are an expert study planner. " +
                "You create clear, concise, practical, day-wise study plans " +
                "with friendly emojis where helpful (for example 📘, ✅, 💡, 🔁), " +
                "but you NEVER include code fences or markdown headings.";

        String priorityInstruction;
        if (days <= 7) {
            priorityInstruction = "CRITICAL: You have only " + days + " days. Focus ONLY on HIGH priority skills. Make it intensive and practical. Skip nice-to-have topics.";
        } else if (days <= 14) {
            priorityInstruction = "You have " + days + " days. Prioritize HIGH priority skills in first week, then MEDIUM priority. Be efficient and focused.";
        } else if (days <= 30) {
            priorityInstruction = "You have " + days + " days. Cover HIGH priority skills thoroughly in first 2 weeks, then MEDIUM priority. Include practice and projects.";
        } else {
            priorityInstruction = "You have " + days + " days (ample time). Cover all HIGH and MEDIUM priority skills systematically. Include deep learning, practice, and projects.";
        }

        String userMessage =
                "Create a strictly structured " + days + "-day study plan for achieving this " + goalType + ": " + topic + ".\n\n" +
                priorityInstruction + "\n\n" +
                gapContext.toString() + "\n" +
                "Rules:\n" +
                "1. " + (days <= 7 ? "CRITICAL: Focus ONLY on HIGH priority skills. Be very intensive." : "Prioritize HIGH priority skills in early days, then MEDIUM.") + "\n" +
                "2. Break strictly day-wise (Day 1, Day 2, etc.).\n" +
                "3. " + (days <= 7 ? "Make each day intensive (2-3 hours minimum)." : "Balance learning with practice.") + "\n" +
                "4. Format each task as: [ ] Task description (use [ ] for checkboxes)\n" +
                "5. Be concise and practical.\n" +
                "6. Use simple emojis and symbols (📘, ✅, 💡).\n" +
                "7. Format: Day X: followed by checkbox tasks [ ] Task 1, [ ] Task 2, etc.\n" +
                "8. No markdown, no tables, just plain text with checkboxes.\n";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("X-Title", "AI Study Planner - Gap-Aware Plan");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(OPENROUTER_CHAT_COMPLETIONS_URL, entity, Map.class);

            Object bodyObj = response.getBody();
            if (!(bodyObj instanceof Map<?, ?> body)) {
                return buildGapAwareOfflinePlan(topic, days, gaps, prioritySkills);
            }

            Object choicesObj = body.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return buildGapAwareOfflinePlan(topic, days, gaps, prioritySkills);
            }

            Object firstChoiceObj = choices.get(0);
            if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
                return buildGapAwareOfflinePlan(topic, days, gaps, prioritySkills);
            }

            Object messageObj = firstChoice.get("message");
            if (!(messageObj instanceof Map<?, ?> message)) {
                return buildGapAwareOfflinePlan(topic, days, gaps, prioritySkills);
            }

            Object contentObj = message.get("content");
            if (contentObj == null) {
                return buildGapAwareOfflinePlan(topic, days, gaps, prioritySkills);
            }

            return contentObj.toString();
        } catch (Exception ignored) {
            return buildGapAwareOfflinePlan(topic, days, gaps, prioritySkills);
        }
    }

    private String buildGapAwareOfflinePlan(String topic, int days, List<com.dilip.studyplan.dto.SkillGap> gaps, List<String> prioritySkills) {
        if (days <= 0) {
            days = 5;
        }

        String normalizedTopic = (topic == null || topic.isBlank())
                ? "your chosen topic"
                : topic.trim();

        StringBuilder sb = new StringBuilder();
        sb.append("📘 Priority-Based Study Plan for ").append(normalizedTopic).append(" (").append(days).append(" days)\n\n");

        // Prioritize skills based on available days
        List<String> focusSkills = prioritySkills != null && !prioritySkills.isEmpty() 
                ? prioritySkills 
                : (gaps != null ? gaps.stream()
                        .filter(g -> "HIGH".equals(g.getPriority()))
                        .map(com.dilip.studyplan.dto.SkillGap::getSkillName)
                        .limit(days <= 7 ? 3 : 5)
                        .toList() : List.of());

        int highPriorityDays = days <= 7 ? days : Math.min(days / 2, 7);
        
        for (int day = 1; day <= days; day++) {
            sb.append("Day ").append(day).append(":\n");

            if (day <= highPriorityDays && !focusSkills.isEmpty()) {
                // High priority phase
                String skill = focusSkills.get(Math.min((day - 1) % focusSkills.size(), focusSkills.size() - 1));
                sb.append("[ ] 🎯 HIGH PRIORITY: Focus on ").append(skill).append("\n");
                sb.append("[ ] 📚 Study fundamentals and core concepts of ").append(skill).append("\n");
                sb.append("[ ] ✅ Practice with hands-on examples\n");
                sb.append("[ ] 📝 Take notes on key points\n\n");
            } else if (day == days) {
                // Final day - review
                sb.append("[ ] 🔁 Review all HIGH priority skills covered\n");
                sb.append("[ ] 🧪 Do a combined practice exercise\n");
                sb.append("[ ] 🎯 Assess your progress and identify remaining gaps\n\n");
            } else {
                // Medium priority or continuation
                if (days > 7 && focusSkills.size() > (day - highPriorityDays - 1)) {
                    String skill = focusSkills.get(Math.min(day - highPriorityDays - 1, focusSkills.size() - 1));
                    sb.append("[ ] 📌 Continue with: ").append(skill).append("\n");
                } else {
                    sb.append("[ ] 📌 Deepen understanding of covered topics\n");
                }
                sb.append("[ ] 📖 Study advanced concepts and best practices\n");
                sb.append("[ ] ✍️ Practice with more complex exercises\n\n");
            }
        }

        sb.append("💡 Tip: ").append(days <= 7 ? "Intensive daily practice (2-3 hours). Focus on HIGH priority only." : "Balance learning with practice. Review regularly.").append("\n");
        return sb.toString();
    }

    /**
     * Pure Java fallback that generates a simple, structured study plan
     * without calling any external AI service.
     */
    private String buildOfflinePlan(String topic, int days) {
        if (days <= 0) {
            days = 5; // sensible default
        }

        String normalizedTopic = (topic == null || topic.isBlank())
                ? "your chosen topic"
                : topic.trim();

        StringBuilder sb = new StringBuilder();
        sb.append("📘 Study Plan for ").append(normalizedTopic).append("\n\n");

        for (int day = 1; day <= days; day++) {
            sb.append("Day ").append(day).append(":\n");

            if (day == 1) {
                sb.append("- 📚 Get an overview of ").append(normalizedTopic).append(".\n");
                sb.append("- ▶️ Watch or read a beginner-friendly introduction.\n");
                sb.append("- 📝 Note down key terms and core concepts.\n\n");
            } else if (day == 2) {
                sb.append("- 🔍 Deepen your understanding of the fundamentals.\n");
                sb.append("- ✅ Work through 2–3 simple examples or exercises.\n");
                sb.append("- 🧠 Write a short summary of what you learned.\n\n");
            } else if (day == days) {
                sb.append("- 🔁 Review all notes and important concepts.\n");
                sb.append("- 🧪 Do a small mini-project or combined exercise.\n");
                sb.append("- 🎯 Reflect on gaps and plan your next steps.\n\n");
            } else {
                sb.append("- 📌 Focus on one sub-topic related to ").append(normalizedTopic).append(".\n");
                sb.append("- 📖 Study from 1–2 quality resources (docs, videos, articles).\n");
                sb.append("- ✍️ Practice with a few targeted exercises.\n\n");
            }
        }

        sb.append("💡 Tip: Keep sessions focused (45–60 minutes) with short breaks.\n");
        return sb.toString();
    }
}

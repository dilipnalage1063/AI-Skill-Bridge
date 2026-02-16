package com.dilip.studyplan.client;

import org.springframework.stereotype.Component;

/**
 * Simplest possible "AI" client: generates a structured study plan
 * locally with no external API calls, no keys, and no network.
 *
 * This makes the project completely self-contained and error-free
 * regardless of internet, quotas, or third-party services.
 */
@Component
public class AIClient {

    public String generateStudyPlan(String topic, int days) {
        if (days <= 0) {
            days = 5; // sensible default
        }

        String normalizedTopic = (topic == null || topic.isBlank())
                ? "your chosen topic"
                : topic.trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Study Plan for ").append(normalizedTopic).append("\n\n");

        for (int day = 1; day <= days; day++) {
            sb.append("Day ").append(day).append(":\n");

            if (day == 1) {
                sb.append("- Get an overview of ").append(normalizedTopic).append(".\n");
                sb.append("- Read or watch a beginner-friendly introduction.\n");
                sb.append("- Note down key terms and core concepts.\n\n");
            } else if (day == 2) {
                sb.append("- Deepen your understanding of the fundamentals.\n");
                sb.append("- Work through 2–3 simple examples or exercises.\n");
                sb.append("- Write a short summary of what you learned.\n\n");
            } else if (day == days) {
                sb.append("- Review all notes and important concepts.\n");
                sb.append("- Do a small mini-project or combined exercise.\n");
                sb.append("- Reflect on gaps and plan your next steps.\n\n");
            } else {
                sb.append("- Focus on one sub-topic related to ").append(normalizedTopic).append(".\n");
                sb.append("- Study from 1–2 quality resources (docs, videos, articles).\n");
                sb.append("- Practice with a few targeted exercises.\n\n");
            }
        }

        sb.append("Tip: Keep sessions focused (45–60 minutes) with short breaks.\n");
        return sb.toString();
    }
}

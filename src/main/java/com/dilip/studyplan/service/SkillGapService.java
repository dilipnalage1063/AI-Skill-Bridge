package com.dilip.studyplan.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dilip.studyplan.client.AIClient;
import com.dilip.studyplan.dto.GapAnalysisResponse;
import com.dilip.studyplan.dto.GoalRequest;
import com.dilip.studyplan.dto.SkillGap;
import com.dilip.studyplan.dto.StudyPlanResponse;
import com.dilip.studyplan.entity.StudyPlan;
import com.dilip.studyplan.repository.StudyPlanRepository;

@Service
public class SkillGapService {

    private final AIClient aiClient;
    private final StudyPlanRepository repository;

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // Common tech skills mapping
    private static final Map<String, List<String>> SKILL_SYNONYMS = createSkillSynonymsMap();

    private static Map<String, List<String>> createSkillSynonymsMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("Java", List.of("java", "j2ee", "j2se", "core java"));
        map.put("Spring Boot", List.of("spring boot", "spring", "spring framework", "spring mvc"));
        map.put("SQL", List.of("sql", "database", "mysql", "postgresql", "oracle", "db"));
        map.put("REST API", List.of("rest", "rest api", "restful", "api", "web services"));
        map.put("Docker", List.of("docker", "containers", "containerization"));
        map.put("AWS", List.of("aws", "amazon web services", "cloud", "ec2", "s3"));
        map.put("DSA", List.of("dsa", "data structures", "algorithms", "algorithm", "leetcode"));
        map.put("System Design", List.of("system design", "distributed systems", "architecture"));
        map.put("JavaScript", List.of("javascript", "js", "ecmascript", "node.js", "nodejs"));
        map.put("React", List.of("react", "reactjs", "react.js"));
        map.put("Python", List.of("python", "django", "flask", "fastapi"));
        map.put("Git", List.of("git", "github", "version control", "gitlab"));
        map.put("Maven", List.of("maven", "mvn", "build tool"));
        return map;
    }

    public SkillGapService(AIClient aiClient, StudyPlanRepository repository) {
        this.aiClient = aiClient;
        this.repository = repository;
    }

    /**
     * Simplified gap analysis: Extract required skills from JD, compare with current skills from resume
     */
    public GapAnalysisResponse analyzeGaps(GoalRequest request) {
        try {
            // 1. Extract required skills from JD/target text
            String jdText = request.getJdText() != null && !request.getJdText().isBlank() 
                    ? request.getJdText() 
                    : (request.getTargetText() != null ? request.getTargetText() : "");
            
            if (jdText == null || jdText.isBlank()) {
                // Return empty response if no JD provided
                GapAnalysisResponse emptyResponse = new GapAnalysisResponse(
                    new ArrayList<>(), new ArrayList<>(), "0%"
                );
                emptyResponse.setAtsScore("N/A");
                emptyResponse.setPrioritizedSkills(new ArrayList<>());
                return emptyResponse;
            }
            
            List<String> requiredSkills = extractRequiredSkills(jdText);

            // 2. Get current skills (from resume + manual input)
            Map<String, Integer> currentSkills = getCurrentSkills(request);

            // 3. Identify gaps
            List<SkillGap> gaps = identifyGaps(requiredSkills, currentSkills);

            // 4. Calculate readiness score
            String readinessScore = calculateReadinessScore(gaps);

            // 5. Calculate ATS score (resume vs JD matching) - handle errors gracefully
            String atsScore = "N/A";
            try {
                atsScore = calculateATSScore(request.getResumeText(), jdText, requiredSkills, currentSkills);
            } catch (Exception e) {
                // If ATS calculation fails, just set to N/A
                atsScore = "N/A";
            }

            // 6. Generate prioritized skills list
            List<String> prioritizedSkills = generatePrioritizedSkills(gaps);

            GapAnalysisResponse response = new GapAnalysisResponse(requiredSkills, gaps, readinessScore);
            response.setAtsScore(atsScore);
            response.setPrioritizedSkills(prioritizedSkills);

            return response;
        } catch (Exception e) {
            // Return a safe error response instead of throwing
            e.printStackTrace(); // Log the error for debugging
            GapAnalysisResponse errorResponse = new GapAnalysisResponse(
                new ArrayList<>(), new ArrayList<>(), "Error"
            );
            errorResponse.setAtsScore("N/A");
            errorResponse.setPrioritizedSkills(new ArrayList<>());
            return errorResponse;
        }
    }

    /**
     * Extract required skills from JD text
     */
    private List<String> extractRequiredSkills(String jdText) {
        if (jdText == null || jdText.isBlank()) {
            return new ArrayList<>();
        }

        // Try AI extraction first (with timeout protection)
        try {
            String prompt = "Extract technical skills and technologies mentioned in this job description. " +
                    "Return ONLY a comma-separated list of skill names (no explanations). " +
                    "Normalize common variations (e.g., 'Java' not 'java').\n\n" + jdText;

            String aiResponse = aiClient.extractSkills(prompt);
            if (aiResponse != null && !aiResponse.isBlank()) {
                List<String> skills = Arrays.stream(aiResponse.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(this::normalizeSkillName)
                        .distinct()
                        .collect(Collectors.toList());
                if (!skills.isEmpty()) {
                    return skills;
                }
            }
        } catch (Exception e) {
            // Fallback to keyword matching - silently continue
        }

        // Fallback: keyword extraction (always works)
        List<String> keywordSkills = extractSkillsByKeywords(jdText);
        if (keywordSkills.isEmpty()) {
            keywordSkills = new ArrayList<>();
            keywordSkills.add("General Skills");
        }
        return keywordSkills;
    }

    /**
     * Get current skills from resume + manual input
     */
    private Map<String, Integer> getCurrentSkills(GoalRequest request) {
        Map<String, Integer> current = new HashMap<>();

        // 1. Manual input takes highest priority
        if (request.getSelfRatings() != null) {
            current.putAll(request.getSelfRatings());
        }

        // 2. Extract from resume if provided
        if (request.getResumeText() != null && !request.getResumeText().isBlank()) {
            Map<String, Integer> resumeSkills = extractSkillsFromResumeText(request.getResumeText());
            // Merge resume skills (don't override manual input)
            for (Map.Entry<String, Integer> entry : resumeSkills.entrySet()) {
                if (!current.containsKey(entry.getKey())) {
                    current.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return current;
    }

    /**
     * Extract skills from resume text using keyword matching
     */
    private Map<String, Integer> extractSkillsFromResumeText(String resumeText) {
        Map<String, Integer> skills = new HashMap<>();
        if (resumeText == null || resumeText.isBlank()) {
            return skills;
        }

        String lowerText = resumeText.toLowerCase();

        for (Map.Entry<String, List<String>> entry : SKILL_SYNONYMS.entrySet()) {
            String skill = entry.getKey();
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    // Simple level inference based on mentions
                    long mentions = lowerText.split(keyword.toLowerCase()).length - 1;
                    int level = mentions > 3 ? 3 : (mentions > 1 ? 2 : 1);
                    skills.put(skill, Math.max(skills.getOrDefault(skill, 0), level));
                    break;
                }
            }
        }

        return skills;
    }

    /**
     * Extract skills using keyword matching
     */
    private List<String> extractSkillsByKeywords(String text) {
        String lowerText = text.toLowerCase();
        Set<String> found = new HashSet<>();

        for (Map.Entry<String, List<String>> entry : SKILL_SYNONYMS.entrySet()) {
            for (String synonym : entry.getValue()) {
                if (lowerText.contains(synonym.toLowerCase())) {
                    found.add(entry.getKey());
                    break;
                }
            }
        }

        return new ArrayList<>(found);
    }

    /**
     * Normalize skill name
     */
    private String normalizeSkillName(String skill) {
        String trimmed = skill.trim();
        if (trimmed.isEmpty()) return trimmed;

        final String normalized = trimmed.substring(0, 1).toUpperCase() +
                trimmed.substring(1).toLowerCase();

        for (String knownSkill : SKILL_SYNONYMS.keySet()) {
            if (knownSkill.equalsIgnoreCase(normalized)) {
                return knownSkill;
            }
        }

        return normalized;
    }

    /**
     * Identify gaps between required and current skills
     */
    private List<SkillGap> identifyGaps(List<String> requiredSkills, Map<String, Integer> currentSkills) {
        List<SkillGap> gaps = new ArrayList<>();

        for (String skill : requiredSkills) {
            int required = 4; // Default required level
            int current = currentSkills.getOrDefault(skill, 0);
            int gapSize = Math.max(0, required - current);

            String priority;
            if (gapSize >= 3) {
                priority = "HIGH";
            } else if (gapSize >= 2) {
                priority = "MEDIUM";
            } else {
                priority = "LOW";
            }

            gaps.add(new SkillGap(skill, required, current, priority));
        }

        return gaps;
    }

    /**
     * Calculate readiness score
     */
    private String calculateReadinessScore(List<SkillGap> gaps) {
        if (gaps.isEmpty()) {
            return "100%";
        }

        int totalCurrentLevels = gaps.stream().mapToInt(SkillGap::getCurrentLevel).sum();
        int totalRequiredLevels = gaps.stream().mapToInt(SkillGap::getRequiredLevel).sum();

        if (totalRequiredLevels == 0) {
            return "100%";
        }

        double readiness = 100.0 * ((double) totalCurrentLevels / totalRequiredLevels);
        readiness = Math.max(0, Math.min(100, readiness));

        return String.format("%.0f%%", readiness);
    }

    /**
     * Calculate ATS (Applicant Tracking System) score based on resume vs JD matching
     * Uses industry-standard ATS scoring logic
     */
    private String calculateATSScore(String resumeText, String jdText, List<String> requiredSkills, Map<String, Integer> currentSkills) {
        if (resumeText == null || resumeText.isBlank() || jdText == null || jdText.isBlank()) {
            return "N/A";
        }

        String resumeLower = resumeText.toLowerCase();

        // ATS scoring factors (industry-standard weights)
        double score = 0.0;

        // 1. Keyword matching (40% weight) - Most important for ATS
        int matchedKeywords = 0;
        int totalKeywords = requiredSkills.size();
        
        for (String skill : requiredSkills) {
            String skillLower = skill.toLowerCase();
            // Check if skill appears in resume
            if (resumeLower.contains(skillLower)) {
                matchedKeywords++;
            } else {
                // Check synonyms
                for (Map.Entry<String, List<String>> entry : SKILL_SYNONYMS.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(skill)) {
                        for (String synonym : entry.getValue()) {
                            if (resumeLower.contains(synonym.toLowerCase())) {
                                matchedKeywords++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        double keywordScore = totalKeywords > 0 ? (matchedKeywords * 40.0 / totalKeywords) : 0;
        score += keywordScore;

        // 2. Skill level matching (30% weight) - How well skills match required levels
        int skillsWithGoodLevel = 0;
        for (String skill : requiredSkills) {
            if (currentSkills.containsKey(skill) && currentSkills.get(skill) >= 3) {
                skillsWithGoodLevel++;
            }
        }
        double levelScore = totalKeywords > 0 ? (skillsWithGoodLevel * 30.0 / totalKeywords) : 0;
        score += levelScore;

        // 3. Experience indicators (20% weight) - Years, projects, certifications
        boolean hasYears = resumeLower.contains("year") || resumeLower.contains("experience");
        boolean hasProjects = resumeLower.contains("project") || resumeLower.contains("developed") || resumeLower.contains("built");
        boolean hasEducation = resumeLower.contains("education") || resumeLower.contains("degree") || resumeLower.contains("bachelor") || resumeLower.contains("master");
        
        double experienceScore = 0;
        if (hasYears) experienceScore += 7;
        if (hasProjects) experienceScore += 7;
        if (hasEducation) experienceScore += 6;
        score += experienceScore;

        // 4. Format and structure (10% weight) - Professional keywords
        boolean hasActionVerbs = resumeLower.contains("developed") || resumeLower.contains("implemented") || 
                                 resumeLower.contains("designed") || resumeLower.contains("managed");
        boolean hasQuantifiableResults = resumeLower.matches(".*\\d+%.*|.*\\d+\\+.*|.*\\d+ years.*");
        
        double formatScore = 0;
        if (hasActionVerbs) formatScore += 5;
        if (hasQuantifiableResults) formatScore += 5;
        score += formatScore;

        // Normalize to 0-100
        score = Math.max(0, Math.min(100, score));

        return String.format("%.0f%%", score);
    }

    /**
     * Generate prioritized skills list based on gaps
     */
    private List<String> generatePrioritizedSkills(List<SkillGap> gaps) {
        return gaps.stream()
                .sorted((a, b) -> {
                    int priorityCompare = b.getPriority().compareTo(a.getPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    return Integer.compare(b.getGapSize(), a.getGapSize());
                })
                .map(SkillGap::getSkillName)
                .collect(Collectors.toList());
    }

    /**
     * Generate study plan from gaps with priority-based scheduling based on available days
     */
    public StudyPlanResponse generatePlanFromGaps(GoalRequest request, GapAnalysisResponse gapAnalysis) {
        // Sort gaps by priority and gap size
        List<SkillGap> sortedGaps = gapAnalysis.getGaps().stream()
                .sorted((a, b) -> {
                    int priorityCompare = b.getPriority().compareTo(a.getPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    return Integer.compare(b.getGapSize(), a.getGapSize());
                })
                .collect(Collectors.toList());

        // Calculate days
        Integer requestDays = request.getDays();
        int days = (requestDays != null && requestDays > 0)
                ? requestDays.intValue()
                : Math.max(7, Math.min(60, sortedGaps.size() * 3));

        // Build prioritized skill list based on available days
        List<String> prioritySkills;
        if (days <= 7) {
            // Very limited time - focus only on HIGH priority, top 3 skills
            prioritySkills = sortedGaps.stream()
                    .filter(g -> "HIGH".equals(g.getPriority()))
                    .limit(3)
                    .map(SkillGap::getSkillName)
                    .collect(Collectors.toList());
        } else if (days <= 14) {
            // Limited time - focus on HIGH priority, top 5 skills
            prioritySkills = sortedGaps.stream()
                    .filter(g -> "HIGH".equals(g.getPriority()))
                    .limit(5)
                    .map(SkillGap::getSkillName)
                    .collect(Collectors.toList());
        } else if (days <= 30) {
            // Moderate time - HIGH + MEDIUM priority, top 7 skills
            prioritySkills = sortedGaps.stream()
                    .filter(g -> "HIGH".equals(g.getPriority()) || "MEDIUM".equals(g.getPriority()))
                    .limit(7)
                    .map(SkillGap::getSkillName)
                    .collect(Collectors.toList());
        } else {
            // Ample time - cover all HIGH and MEDIUM priority skills
            prioritySkills = sortedGaps.stream()
                    .filter(g -> "HIGH".equals(g.getPriority()) || "MEDIUM".equals(g.getPriority()))
                    .map(SkillGap::getSkillName)
                    .collect(Collectors.toList());
        }

        // Fallback if no priority skills found
        if (prioritySkills.isEmpty()) {
            prioritySkills = sortedGaps.stream()
                    .limit(Math.min(5, sortedGaps.size()))
                    .map(SkillGap::getSkillName)
                    .collect(Collectors.toList());
        }

        StringBuilder topicBuilder = new StringBuilder();
        topicBuilder.append("Master skills for ").append(request.getGoalType()).append(": ");
        topicBuilder.append(String.join(", ", prioritySkills));

        // Generate plan with priority-aware scheduling
        String planContent = aiClient.generateGapAwarePlan(
                topicBuilder.toString(),
                days,
                gapAnalysis.getGaps(),
                request.getGoalType(),
                prioritySkills
        );

        // Save plan
        StudyPlan entity = new StudyPlan();
        entity.setTopic(topicBuilder.toString());
        entity.setPlan(planContent);
        entity.setGeneratedAt(LocalDateTime.now());

        StudyPlan saved = repository.save(entity);

        return new StudyPlanResponse(
                saved.getId(),
                saved.getTopic(),
                saved.getPlan(),
                saved.getGeneratedAt().format(DISPLAY_FORMATTER)
        );
    }
}

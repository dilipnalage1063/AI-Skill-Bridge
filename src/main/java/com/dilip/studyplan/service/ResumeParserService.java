package com.dilip.studyplan.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Service to parse resume files and extract skills
 */
@Service
public class ResumeParserService {

    private final Tika tika;
    
    // Common technical skills keywords
    private static final Map<String, List<String>> SKILL_KEYWORDS = createSkillKeywords();

    public ResumeParserService() {
        this.tika = new Tika();
    }

    /**
     * Extract text content from uploaded resume file
     */
    public String extractTextFromResume(MultipartFile file) throws IOException, TikaException {
        if (file == null || file.isEmpty()) {
            return "";
        }

        try (InputStream inputStream = file.getInputStream()) {
            String text = tika.parseToString(inputStream);
            return text != null ? text : "";
        }
    }

    /**
     * Extract skills from resume text using AI and keyword matching
     */
    public Map<String, Integer> extractSkillsFromResume(String resumeText, com.dilip.studyplan.client.AIClient aiClient) {
        if (resumeText == null || resumeText.isBlank()) {
            return new HashMap<>();
        }

        Map<String, Integer> skills = new HashMap<>();

        // Try AI extraction first
        try {
            String prompt = "Extract technical skills and technologies mentioned in this resume. " +
                    "Return ONLY a comma-separated list of skill names (no explanations, no numbers, just names). " +
                    "Normalize common variations (e.g., 'Java' not 'java' or 'JAVA'). " +
                    "Focus on technical skills only.\n\n" + resumeText;

            String aiResponse = aiClient.extractSkills(prompt);
            if (aiResponse != null && !aiResponse.isBlank()) {
                String[] extractedSkills = aiResponse.split(",");
                for (String skill : extractedSkills) {
                    String normalized = normalizeSkillName(skill.trim());
                    if (!normalized.isEmpty()) {
                        // Infer skill level based on context (mentions, years of experience, etc.)
                        int level = inferSkillLevelFromResume(resumeText, normalized);
                        skills.put(normalized, level);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to keyword matching
        }

        // Fallback: keyword-based extraction
        if (skills.isEmpty()) {
            skills = extractSkillsByKeywords(resumeText);
        }

        return skills;
    }

    /**
     * Infer skill level from resume text (mentions, years, projects, etc.)
     */
    private int inferSkillLevelFromResume(String resumeText, String skill) {
        String lowerText = resumeText.toLowerCase();
        String lowerSkill = skill.toLowerCase();

        // Count mentions
        long mentions = lowerText.split(lowerSkill).length - 1;

        // Check for experience indicators
        boolean hasYears = lowerText.contains("years") && lowerText.contains(lowerSkill);
        boolean hasProjects = lowerText.contains("project") && lowerText.contains(lowerSkill);
        boolean hasCertification = lowerText.contains("certified") || lowerText.contains("certification");

        // Simple heuristic
        if (hasYears || mentions > 5) {
            return 4; // Experienced
        } else if (hasProjects || mentions > 2) {
            return 3; // Intermediate
        } else if (mentions > 0 || hasCertification) {
            return 2; // Basic
        }

        return 1; // Mentioned but minimal
    }

    /**
     * Extract skills using keyword matching
     */
    private Map<String, Integer> extractSkillsByKeywords(String resumeText) {
        Map<String, Integer> skills = new HashMap<>();
        String lowerText = resumeText.toLowerCase();

        for (Map.Entry<String, List<String>> entry : SKILL_KEYWORDS.entrySet()) {
            String skill = entry.getKey();
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    int level = inferSkillLevelFromResume(resumeText, skill);
                    skills.put(skill, Math.max(skills.getOrDefault(skill, 0), level));
                    break;
                }
            }
        }

        return skills;
    }

    /**
     * Normalize skill name
     */
    private String normalizeSkillName(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            return "";
        }
        String normalized = skill.trim();
        if (normalized.isEmpty()) return "";
        
        normalized = normalized.substring(0, 1).toUpperCase() + 
                     normalized.substring(1).toLowerCase();

        // Check if it matches any known skill
        for (String knownSkill : SKILL_KEYWORDS.keySet()) {
            if (knownSkill.equalsIgnoreCase(normalized)) {
                return knownSkill;
            }
        }

        return normalized;
    }

    private static Map<String, List<String>> createSkillKeywords() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("Java", List.of("java", "j2ee", "j2se", "core java", "java ee"));
        map.put("Spring Boot", List.of("spring boot", "spring", "spring framework", "spring mvc", "spring cloud"));
        map.put("SQL", List.of("sql", "database", "mysql", "postgresql", "oracle", "db", "rdbms"));
        map.put("REST API", List.of("rest", "rest api", "restful", "api", "web services", "microservices"));
        map.put("Docker", List.of("docker", "containers", "containerization", "dockerfile"));
        map.put("AWS", List.of("aws", "amazon web services", "cloud", "ec2", "s3", "lambda", "rds"));
        map.put("DSA", List.of("dsa", "data structures", "algorithms", "algorithm", "leetcode", "competitive programming"));
        map.put("System Design", List.of("system design", "distributed systems", "architecture", "microservices architecture"));
        map.put("JavaScript", List.of("javascript", "js", "ecmascript", "node.js", "nodejs", "es6"));
        map.put("React", List.of("react", "reactjs", "react.js", "react native"));
        map.put("Python", List.of("python", "django", "flask", "fastapi", "pandas", "numpy"));
        map.put("Git", List.of("git", "github", "version control", "gitlab", "bitbucket"));
        map.put("Maven", List.of("maven", "mvn", "build tool"));
        map.put("Kubernetes", List.of("kubernetes", "k8s", "container orchestration"));
        map.put("MongoDB", List.of("mongodb", "nosql", "document database"));
        map.put("Redis", List.of("redis", "cache", "caching"));
        return map;
    }
}

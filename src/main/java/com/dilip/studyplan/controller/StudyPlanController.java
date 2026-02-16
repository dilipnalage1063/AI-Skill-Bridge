package com.dilip.studyplan.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dilip.studyplan.dto.GapAnalysisResponse;
import com.dilip.studyplan.dto.GoalRequest;
import com.dilip.studyplan.dto.StudyPlanRequest;
import com.dilip.studyplan.dto.StudyPlanResponse;
import com.dilip.studyplan.service.ResumeParserService;
import com.dilip.studyplan.service.SkillGapService;
import com.dilip.studyplan.service.StudyPlanService;

@RestController
@RequestMapping("/api/study")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final SkillGapService skillGapService;
    private final ResumeParserService resumeParserService;

    public StudyPlanController(StudyPlanService studyPlanService, SkillGapService skillGapService, ResumeParserService resumeParserService) {
        this.studyPlanService = studyPlanService;
        this.skillGapService = skillGapService;
        this.resumeParserService = resumeParserService;
    }

    @PostMapping("/generate")
    public StudyPlanResponse generatePlan(@RequestBody StudyPlanRequest request) {
        return studyPlanService.generatePlan(request);
    }

    @GetMapping("/history")
    public List<StudyPlanResponse> getHistory() {
        return studyPlanService.getHistory();
    }
    
    @PostMapping("/analyze-gap")
    public GapAnalysisResponse analyzeGap(@RequestBody GoalRequest goalRequest) {
        return skillGapService.analyzeGaps(goalRequest);
    }
    
    @PostMapping("/upload-resume")
    public Map<String, Object> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            String resumeText = resumeParserService.extractTextFromResume(file);
            Map<String, Integer> skills = resumeParserService.extractSkillsFromResume(resumeText, null);
            
            return Map.of(
                "success", true,
                "resumeText", resumeText,
                "skills", skills,
                "message", "Resume parsed successfully. Found " + skills.size() + " skills."
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage() != null ? e.getMessage() : "Failed to parse resume"
            );
        }
    }
    
    @PostMapping("/upload-jd")
    public Map<String, Object> uploadJD(@RequestParam("file") MultipartFile file) {
        try {
            String jdText = resumeParserService.extractTextFromResume(file); // Reuse same parser
            
            return Map.of(
                "success", true,
                "jdText", jdText,
                "message", "Job description parsed successfully."
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage() != null ? e.getMessage() : "Failed to parse job description"
            );
        }
    }
}

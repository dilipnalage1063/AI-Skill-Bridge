package com.dilip.studyplan.dto;

import java.util.List;

public class GapAnalysisResponse {

    private List<String> requiredSkills; // Skills required for the job/exam
    private List<SkillGap> gaps; // Skill gaps identified
    private String readinessScore; // Overall readiness percentage (0-100%)
    private String atsScore; // ATS (Applicant Tracking System) score percentage
    private List<String> prioritizedSkills; // Skills prioritized based on gaps

    public GapAnalysisResponse() {
    }
    
    public GapAnalysisResponse(List<String> requiredSkills, List<SkillGap> gaps, String readinessScore) {
        this.requiredSkills = requiredSkills;
        this.gaps = gaps;
        this.readinessScore = readinessScore;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<SkillGap> getGaps() {
        return gaps;
    }

    public void setGaps(List<SkillGap> gaps) {
        this.gaps = gaps;
    }

    public String getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(String readinessScore) {
        this.readinessScore = readinessScore;
    }
    
    public String getAtsScore() {
        return atsScore;
    }
    
    public void setAtsScore(String atsScore) {
        this.atsScore = atsScore;
    }
    
    public List<String> getPrioritizedSkills() {
        return prioritizedSkills;
    }
    
    public void setPrioritizedSkills(List<String> prioritizedSkills) {
        this.prioritizedSkills = prioritizedSkills;
    }
}

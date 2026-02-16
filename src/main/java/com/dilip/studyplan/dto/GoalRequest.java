package com.dilip.studyplan.dto;

import java.util.Map;

public class GoalRequest {

    private String goalType; // "JOB", "EXAM", "CUSTOM"
    private String targetText; // Job description, exam syllabus, or custom goal text (can be from JD upload)
    private Map<String, Integer> selfRatings; // Skill name -> rating (0-5) - from resume or manual input
    private Integer days; // Optional: desired study duration
    private String resumeText; // Extracted text from uploaded resume
    private String jdText; // Extracted text from uploaded JD

    public GoalRequest() {
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public String getTargetText() {
        return targetText;
    }

    public void setTargetText(String targetText) {
        this.targetText = targetText;
    }

    public Map<String, Integer> getSelfRatings() {
        return selfRatings;
    }

    public void setSelfRatings(Map<String, Integer> selfRatings) {
        this.selfRatings = selfRatings;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
    
    public String getResumeText() {
        return resumeText;
    }
    
    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }
    
    public String getJdText() {
        return jdText;
    }
    
    public void setJdText(String jdText) {
        this.jdText = jdText;
    }
}

package com.dilip.studyplan.dto;

public class SkillGap {

    private String skillName;
    private int requiredLevel; // 0-5
    private int currentLevel; // 0-5
    private String priority; // "HIGH", "MEDIUM", "LOW"
    private int gapSize; // requiredLevel - currentLevel

    public SkillGap() {
    }

    public SkillGap(String skillName, int requiredLevel, int currentLevel, String priority) {
        this.skillName = skillName;
        this.requiredLevel = requiredLevel;
        this.currentLevel = currentLevel;
        this.priority = priority;
        this.gapSize = Math.max(0, requiredLevel - currentLevel);
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
        this.gapSize = Math.max(0, requiredLevel - currentLevel);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
        this.gapSize = Math.max(0, requiredLevel - currentLevel);
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getGapSize() {
        return gapSize;
    }

    public void setGapSize(int gapSize) {
        this.gapSize = gapSize;
    }
}

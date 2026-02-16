package com.dilip.studyplan.dto;

public class StudyPlanResponse {

    private Long id;
    private String topic;
    private String plan;
    private String generatedAt;

    public StudyPlanResponse() {
    }

    public StudyPlanResponse(Long id, String topic, String plan, String generatedAt) {
        this.id = id;
        this.topic = topic;
        this.plan = plan;
        this.generatedAt = generatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}

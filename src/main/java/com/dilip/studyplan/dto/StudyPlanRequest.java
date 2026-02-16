package com.dilip.studyplan.dto;

public class StudyPlanRequest {

    private String topic;
    private int days;

    public StudyPlanRequest() {
    }

    public StudyPlanRequest(String topic, int days) {
        this.topic = topic;
        this.days = days;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}

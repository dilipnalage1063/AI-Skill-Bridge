package com.dilip.studyplan.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import com.dilip.studyplan.client.AIClient;
import com.dilip.studyplan.dto.StudyPlanRequest;
import com.dilip.studyplan.dto.StudyPlanResponse;
import com.dilip.studyplan.entity.StudyPlan;
import com.dilip.studyplan.repository.StudyPlanRepository;

@Service
public class StudyPlanService {

    private final AIClient aiClient;
    private final StudyPlanRepository repository;

    public StudyPlanService(AIClient aiClient, StudyPlanRepository repository) {
        this.aiClient = aiClient;
        this.repository = repository;
    }

    public StudyPlanResponse generatePlan(StudyPlanRequest request) {

        String aiResponse = aiClient.generateStudyPlan(
                request.getTopic(),
                request.getDays()
        );

        // Save entity
        StudyPlan entity = new StudyPlan();
        entity.setTopic(request.getTopic());
        entity.setPlan(aiResponse);
        entity.setGeneratedAt(LocalDateTime.now());

        StudyPlan saved = repository.save(entity);

        // Convert Entity → DTO (including ID)
        return new StudyPlanResponse(
                saved.getId(),
                saved.getTopic(),
                saved.getPlan(),
                saved.getGeneratedAt().toString()
        );
    }

    public List<StudyPlanResponse> getHistory() {

        return repository.findAll()
                .stream()
                .map(entity -> new StudyPlanResponse(
                        entity.getId(),
                        entity.getTopic(),
                        entity.getPlan(),
                        entity.getGeneratedAt().toString()
                ))
                .toList();
    }

    public void deletePlan(Long id) {
        repository.deleteById(id);
    }
}

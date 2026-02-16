package com.dilip.studyplan.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dilip.studyplan.client.AIClient;
import com.dilip.studyplan.dto.StudyPlanRequest;
import com.dilip.studyplan.dto.StudyPlanResponse;
import com.dilip.studyplan.entity.StudyPlan;
import com.dilip.studyplan.repository.StudyPlanRepository;

@Service
public class StudyPlanService {

    private final AIClient aiClient;
    private final StudyPlanRepository repository;

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

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
                saved.getGeneratedAt().format(DISPLAY_FORMATTER)
        );
    }

    public List<StudyPlanResponse> getHistory() {

        return repository.findAll()
                .stream()
                .map(entity -> new StudyPlanResponse(
                        entity.getId(),
                        entity.getTopic(),
                        entity.getPlan(),
                        entity.getGeneratedAt().format(DISPLAY_FORMATTER)
                ))
                .toList();
    }

    public StudyPlanResponse getPlan(Long id) {
        StudyPlan entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found: " + id));

        return new StudyPlanResponse(
                entity.getId(),
                entity.getTopic(),
                entity.getPlan(),
                entity.getGeneratedAt().format(DISPLAY_FORMATTER)
        );
    }

    public void deletePlan(Long id) {
        repository.deleteById(id);
    }
}

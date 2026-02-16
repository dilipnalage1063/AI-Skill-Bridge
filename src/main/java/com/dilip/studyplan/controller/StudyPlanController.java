package com.dilip.studyplan.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.dilip.studyplan.dto.StudyPlanRequest;
import com.dilip.studyplan.dto.StudyPlanResponse;
import com.dilip.studyplan.service.StudyPlanService;

@RestController
@RequestMapping("/api/study")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @PostMapping("/generate")
    public StudyPlanResponse generatePlan(@RequestBody StudyPlanRequest request) {
        return studyPlanService.generatePlan(request);
    }

    @GetMapping("/history")
    public List<StudyPlanResponse> getHistory() {
        return studyPlanService.getHistory();
    }
}

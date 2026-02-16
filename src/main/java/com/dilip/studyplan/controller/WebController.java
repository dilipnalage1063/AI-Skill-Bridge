package com.dilip.studyplan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dilip.studyplan.dto.GapAnalysisResponse;
import com.dilip.studyplan.dto.GoalRequest;
import com.dilip.studyplan.dto.StudyPlanResponse;
import com.dilip.studyplan.service.SkillGapService;
import com.dilip.studyplan.service.StudyPlanService;

@Controller
public class WebController {

    private final StudyPlanService service;
    private final SkillGapService skillGapService;

    public WebController(StudyPlanService service, SkillGapService skillGapService) {
        this.service = service;
        this.skillGapService = skillGapService;
    }

    @GetMapping("/")
    public String home(Model model) {
        GoalRequest goalRequest = new GoalRequest();
        goalRequest.setGoalType("JOB"); // Default to JOB
        model.addAttribute("goalRequest", goalRequest);
        model.addAttribute("history", service.getHistory());
        return "index";
    }

    @GetMapping("/history/{id}")
    public String viewHistoryPlan(@PathVariable Long id, Model model) {
        StudyPlanResponse response = service.getPlan(id);
        
        GoalRequest goalRequest = new GoalRequest();
        goalRequest.setGoalType("JOB");
        model.addAttribute("goalRequest", goalRequest);
        model.addAttribute("response", response);
        model.addAttribute("history", service.getHistory());

        return "index";
    }
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.deletePlan(id);
        return "redirect:/";
    }

    @PostMapping("/analyze-gap")
    public String analyzeGap(@ModelAttribute GoalRequest goalRequest, Model model) {
        try {
            // Ensure goalType is JOB
            if (goalRequest.getGoalType() == null || goalRequest.getGoalType().isBlank()) {
                goalRequest.setGoalType("JOB");
            }
            
            // Validate that at least JD text or target text is provided
            String jdText = goalRequest.getJdText() != null && !goalRequest.getJdText().isBlank() 
                    ? goalRequest.getJdText() 
                    : goalRequest.getTargetText();
            
            if (jdText == null || jdText.isBlank()) {
                // Return to form with error message
                GoalRequest newRequest = new GoalRequest();
                newRequest.setGoalType("JOB");
                model.addAttribute("goalRequest", newRequest);
                model.addAttribute("error", "Please provide a job description either by uploading a file or pasting the text.");
                model.addAttribute("history", service.getHistory());
                return "index";
            }
            
            GapAnalysisResponse gapAnalysis = skillGapService.analyzeGaps(goalRequest);
            
            // Preserve resume and JD data for plan generation
            GoalRequest preservedRequest = new GoalRequest();
            preservedRequest.setGoalType("JOB");
            preservedRequest.setTargetText(goalRequest.getTargetText());
            preservedRequest.setResumeText(goalRequest.getResumeText());
            preservedRequest.setJdText(goalRequest.getJdText());
            preservedRequest.setSelfRatings(goalRequest.getSelfRatings());
            
            model.addAttribute("goalRequest", preservedRequest);
            model.addAttribute("gapAnalysis", gapAnalysis);
            model.addAttribute("history", service.getHistory());
            
            return "index";
        } catch (Exception e) {
            // Log error and return safe state
            e.printStackTrace();
            GoalRequest newRequest = new GoalRequest();
            newRequest.setGoalType("JOB");
            model.addAttribute("goalRequest", newRequest);
            model.addAttribute("error", "An error occurred while analyzing gaps. Please try again.");
            model.addAttribute("history", service.getHistory());
            return "index";
        }
    }

    @PostMapping("/generate-plan-from-gap")
    public String generatePlanFromGap(@ModelAttribute GoalRequest goalRequest, Model model) {
        // Ensure goalType is JOB
        if (goalRequest.getGoalType() == null || goalRequest.getGoalType().isBlank()) {
            goalRequest.setGoalType("JOB");
        }
        
        GapAnalysisResponse gapAnalysis = skillGapService.analyzeGaps(goalRequest);
        StudyPlanResponse planResponse = skillGapService.generatePlanFromGaps(goalRequest, gapAnalysis);
        
        GoalRequest newGoalRequest = new GoalRequest();
        newGoalRequest.setGoalType("JOB");
        model.addAttribute("goalRequest", newGoalRequest);
        model.addAttribute("response", planResponse);
        model.addAttribute("gapAnalysis", gapAnalysis);
        model.addAttribute("history", service.getHistory());
        
        return "index";
    }

}

package com.dilip.studyplan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dilip.studyplan.dto.StudyPlanRequest;
import com.dilip.studyplan.dto.StudyPlanResponse;
import com.dilip.studyplan.service.StudyPlanService;

@Controller
public class WebController {

    private final StudyPlanService service;

    public WebController(StudyPlanService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("request", new StudyPlanRequest());
        model.addAttribute("history", service.getHistory());
        return "index";
    }

    @PostMapping("/generate-plan")
    public String generatePlan(@ModelAttribute StudyPlanRequest request, Model model) {

        StudyPlanResponse response = service.generatePlan(request);

        model.addAttribute("request", new StudyPlanRequest());  // IMPORTANT
        model.addAttribute("response", response);
        model.addAttribute("history", service.getHistory());

        return "index";
    }
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.deletePlan(id);
        return "redirect:/";
    }

}

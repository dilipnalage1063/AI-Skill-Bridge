package com.dilip.studyplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dilip.studyplan.entity.StudyPlan;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
	void deleteById(Long id);
}

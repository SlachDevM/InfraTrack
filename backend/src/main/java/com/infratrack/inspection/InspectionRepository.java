package com.infratrack.inspection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    List<Inspection> findAllByOrderByCreatedAtDesc();

    boolean existsByBusinessTriggerIdAndStatus(Long businessTriggerId, InspectionStatus status);
}

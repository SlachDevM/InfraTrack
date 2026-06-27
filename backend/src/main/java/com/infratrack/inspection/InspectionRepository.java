package com.infratrack.inspection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    List<Inspection> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    Page<Inspection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByBusinessTriggerIdAndStatus(Long businessTriggerId, InspectionStatus status);
}

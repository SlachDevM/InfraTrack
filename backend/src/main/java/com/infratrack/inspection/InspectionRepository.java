package com.infratrack.inspection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    List<Inspection> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    Page<Inspection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByBusinessTriggerIdAndStatus(Long businessTriggerId, InspectionStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.department", "businessTrigger"})
    @Query("""
            SELECT i FROM Inspection i
            WHERE i.status = :status
              AND i.issueIdentified = true
              AND i.completedByUserId = :userId
              AND i.asset.department.id = :departmentId
              AND NOT EXISTS (SELECT 1 FROM Issue issue WHERE issue.inspection.id = i.id)
            """)
    Page<Inspection> findEligibleForIssueRecording(
            @Param("status") InspectionStatus status,
            @Param("userId") Long userId,
            @Param("departmentId") Long departmentId,
            Pageable pageable);
}

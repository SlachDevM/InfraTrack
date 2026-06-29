package com.infratrack.preventivemaintenance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PreventiveExecutionReportRepository extends JpaRepository<PreventiveExecutionReport, Long> {

    @EntityGraph(attributePaths = {"candidate", "candidate.asset"})
    Optional<PreventiveExecutionReport> findByCandidateId(Long candidateId);

    @EntityGraph(attributePaths = {"candidate", "candidate.asset"})
    Optional<PreventiveExecutionReport> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"candidate", "candidate.asset"})
    @Query("""
            SELECT r FROM PreventiveExecutionReport r
            WHERE (:status IS NULL OR r.reportStatus = :status)
              AND (:assetId IS NULL OR r.assetIdSnapshot = :assetId)
              AND (:planId IS NULL OR r.preventiveMaintenancePlanIdSnapshot = :planId)
              AND (:decisionSource IS NULL OR r.decisionSource = :decisionSource)
            """)
    Page<PreventiveExecutionReport> findFiltered(
            @Param("status") ExecutionReportStatus status,
            @Param("assetId") Long assetId,
            @Param("planId") Long planId,
            @Param("decisionSource") DecisionSource decisionSource,
            Pageable pageable);
}

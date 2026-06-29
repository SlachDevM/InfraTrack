package com.infratrack.preventivemaintenance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PreventiveExecutionCandidateRepository
        extends JpaRepository<PreventiveExecutionCandidate, Long> {

    @EntityGraph(attributePaths = {
            "preventiveMaintenancePlan",
            "preventiveMaintenancePlan.inspectionTemplate",
            "asset"
    })
    Optional<PreventiveExecutionCandidate> findDetailedById(Long id);

    Optional<PreventiveExecutionCandidate> findByPreventiveMaintenancePlanIdAndCandidateStatus(
            Long preventiveMaintenancePlanId,
            ExecutionCandidateStatus candidateStatus);

    @EntityGraph(attributePaths = {"preventiveMaintenancePlan", "asset"})
    @Query("""
            SELECT c FROM PreventiveExecutionCandidate c
            WHERE (:status IS NULL OR c.candidateStatus = :status)
              AND (:assetId IS NULL OR c.asset.id = :assetId)
              AND (:planId IS NULL OR c.preventiveMaintenancePlan.id = :planId)
            """)
    Page<PreventiveExecutionCandidate> findFiltered(
            @Param("status") ExecutionCandidateStatus status,
            @Param("assetId") Long assetId,
            @Param("planId") Long planId,
            Pageable pageable);
}

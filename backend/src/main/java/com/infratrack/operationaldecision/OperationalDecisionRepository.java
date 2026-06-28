package com.infratrack.operationaldecision;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OperationalDecisionRepository extends JpaRepository<OperationalDecision, Long> {

    List<OperationalDecision> findAllByOrderByCreatedAtDesc();

    Page<OperationalDecision> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByIssueId(Long issueId);

    @EntityGraph(attributePaths = {"asset", "asset.department", "issue"})
    Optional<OperationalDecision> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"asset", "asset.department", "issue"})
    List<OperationalDecision> findAllByAsset_IdOrderByDecidedAtDesc(Long assetId);

    @EntityGraph(attributePaths = {"asset", "asset.department", "issue"})
    @Query("""
            SELECT od FROM OperationalDecision od
            WHERE od.outcome IN (
                com.infratrack.operationaldecision.OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                com.infratrack.operationaldecision.OperationalDecisionOutcome.CONTRACTOR_WORK
            )
              AND NOT EXISTS (SELECT 1 FROM WorkOrder wo WHERE wo.operationalDecision.id = od.id)
              AND :coordinatorDepartmentId IS NOT NULL
              AND od.asset.department.id = :coordinatorDepartmentId
            """)
    Page<OperationalDecision> findEligibleForWorkOrderCreation(
            @Param("coordinatorDepartmentId") Long coordinatorDepartmentId,
            Pageable pageable);
}

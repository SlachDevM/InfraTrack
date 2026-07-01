package com.infratrack.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findAllByOrderByCreatedAtDesc();

    Page<Issue> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByInspectionId(Long inspectionId);

    boolean existsBySourceCompletionReviewId(Long sourceCompletionReviewId);

    @EntityGraph(attributePaths = {"asset", "asset.department", "inspection", "sourceCompletionReview"})
    Optional<Issue> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"asset", "asset.department", "inspection", "sourceCompletionReview"})
    List<Issue> findAllByAsset_IdOrderByRecordedAtDesc(Long assetId);

    @EntityGraph(attributePaths = {"asset", "asset.department", "inspection", "sourceCompletionReview"})
    @Query("""
            SELECT i FROM Issue i
            WHERE NOT EXISTS (SELECT 1 FROM OperationalDecision od WHERE od.issue.id = i.id)
              AND (
                i.issueType = com.infratrack.issue.IssueType.REWORK
                OR i.inspection.status = com.infratrack.inspection.InspectionStatus.COMPLETED
              )
              AND (
                (:managerDepartmentId IS NOT NULL AND i.asset.department.id = :managerDepartmentId)
                OR EXISTS (
                  SELECT 1 FROM DelegatedAuthority d
                  WHERE d.delegateManagerUserId = :managerId
                    AND d.targetDepartment.id = i.asset.department.id
                    AND d.revoked = false
                    AND d.validFrom <= :at
                    AND d.validUntil > :at
                )
              )
            """)
    Page<Issue> findEligibleForOperationalDecision(
            @Param("managerId") Long managerId,
            @Param("managerDepartmentId") Long managerDepartmentId,
            @Param("at") LocalDateTime at,
            Pageable pageable);

    @EntityGraph(attributePaths = {"asset", "asset.department"})
    @Query("""
            SELECT i FROM Issue i
            WHERE (:departmentId IS NULL OR i.asset.department.id = :departmentId)
              AND (:from IS NULL OR i.recordedAt >= :from)
              AND (:to IS NULL OR i.recordedAt < :to)
            ORDER BY i.recordedAt DESC
            """)
    List<Issue> findForExport(
            @Param("departmentId") Long departmentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}

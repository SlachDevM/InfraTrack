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

    @EntityGraph(attributePaths = {"asset", "asset.department", "inspection"})
    Optional<Issue> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"asset", "asset.department", "inspection"})
    @Query("""
            SELECT i FROM Issue i
            WHERE NOT EXISTS (SELECT 1 FROM OperationalDecision od WHERE od.issue.id = i.id)
              AND i.inspection.status = com.infratrack.inspection.InspectionStatus.COMPLETED
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
}

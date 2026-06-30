package com.infratrack.maintenanceactivity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MaintenanceActivityRepository extends JpaRepository<MaintenanceActivity, Long> {

    boolean existsByWorkOrderId(Long workOrderId);

    Optional<MaintenanceActivity> findByWorkOrderId(Long workOrderId);

    @Query("""
            SELECT COUNT(ma) FROM MaintenanceActivity ma
            WHERE ma.performedByUserId = :userId
              AND ma.completedAt >= :start
              AND ma.completedAt < :end
            """)
    long countCompletedByUserBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<MaintenanceActivity> findAllByOrderByCompletedAtDesc();

    @EntityGraph(attributePaths = {"asset", "asset.department", "workOrder"})
    @Query("""
            SELECT ma FROM MaintenanceActivity ma
            WHERE ma.workOrder.status = com.infratrack.workorder.WorkOrderStatus.COMPLETED
              AND NOT EXISTS (
                SELECT 1 FROM CompletionReview cr WHERE cr.maintenanceActivity.id = ma.id
              )
              AND (
                (:managerDepartmentId IS NOT NULL AND ma.asset.department.id = :managerDepartmentId)
                OR EXISTS (
                  SELECT 1 FROM DelegatedAuthority d
                  WHERE d.delegateManagerUserId = :managerId
                    AND d.targetDepartment.id = ma.asset.department.id
                    AND d.revoked = false
                    AND d.validFrom <= :at
                    AND d.validUntil > :at
                )
              )
            ORDER BY ma.completedAt DESC
            """)
    List<MaintenanceActivity> findEligibleForCompletionReview(
            @Param("managerId") Long managerId,
            @Param("managerDepartmentId") Long managerDepartmentId,
            @Param("at") LocalDateTime at);

    @EntityGraph(attributePaths = {"workOrder", "asset"})
    List<MaintenanceActivity> findAllByAsset_IdOrderByCompletedAtDesc(Long assetId);
}

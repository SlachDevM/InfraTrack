package com.infratrack.workorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    List<WorkOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    Page<WorkOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    Optional<WorkOrder> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    List<WorkOrder> findAllByAsset_IdOrderByCreatedAtDesc(Long assetId);

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    @Query("""
            SELECT wo FROM WorkOrder wo
            WHERE wo.status = com.infratrack.workorder.WorkOrderStatus.CREATED
              AND :coordinatorDepartmentId IS NOT NULL
              AND wo.asset.department.id = :coordinatorDepartmentId
            """)
    Page<WorkOrder> findEligibleForAssignment(
            @Param("coordinatorDepartmentId") Long coordinatorDepartmentId,
            Pageable pageable);

    boolean existsByOperationalDecisionId(Long operationalDecisionId);

    @EntityGraph(attributePaths = {
            "asset", "asset.department", "asset.assetCategory",
            "operationalDecision", "operationalDecision.issue"})
    Optional<WorkOrder> findMobileBundleById(Long id);

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    List<WorkOrder> findByAssignedToUserId(Long assignedToUserId);

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    List<WorkOrder> findByAsset_Department_Id(Long departmentId);

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    List<WorkOrder> findByStatus(WorkOrderStatus status);

    long countByAssignedToUserIdAndStatus(Long assignedToUserId, WorkOrderStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.department"})
    @Query("""
            SELECT wo FROM WorkOrder wo
            WHERE (:departmentId IS NULL OR wo.asset.department.id = :departmentId)
              AND (:from IS NULL OR wo.createdAt >= :from)
              AND (:to IS NULL OR wo.createdAt <= :to)
            ORDER BY wo.createdAt DESC
            """)
    List<WorkOrder> findForExport(
            @Param("departmentId") Long departmentId,
            @Param("from") Long from,
            @Param("to") Long to);
}

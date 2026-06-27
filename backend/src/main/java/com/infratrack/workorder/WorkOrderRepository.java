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
}

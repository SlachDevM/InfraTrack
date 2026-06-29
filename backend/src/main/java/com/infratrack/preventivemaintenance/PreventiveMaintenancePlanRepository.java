package com.infratrack.preventivemaintenance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface PreventiveMaintenancePlanRepository extends JpaRepository<PreventiveMaintenancePlan, Long> {

    boolean existsByPlanCode(String planCode);

    @EntityGraph(attributePaths = {"asset", "businessTrigger", "inspectionTemplate"})
    @Query("""
            SELECT p FROM PreventiveMaintenancePlan p
            LEFT JOIN p.businessTrigger t
            WHERE (:assetId IS NULL OR p.asset.id = :assetId)
              AND (:status IS NULL OR p.status = :status)
              AND (:triggerType IS NULL OR t.triggerType = :triggerType)
            """)
    Page<PreventiveMaintenancePlan> findFiltered(
            @Param("assetId") Long assetId,
            @Param("status") PreventiveMaintenancePlanStatus status,
            @Param("triggerType") PlanTriggerType triggerType,
            Pageable pageable);

    @EntityGraph(attributePaths = {"asset", "businessTrigger", "inspectionTemplate"})
    Optional<PreventiveMaintenancePlan> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    List<PreventiveMaintenancePlan> findAllByStatus(PreventiveMaintenancePlanStatus status);

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    List<PreventiveMaintenancePlan> findAllByStatusAndAsset_Department_Id(
            PreventiveMaintenancePlanStatus status,
            Long departmentId);
}

package com.infratrack.suggestedaction;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SuggestedActionRepository extends JpaRepository<SuggestedAction, Long> {

    @Query("""
            SELECT sa FROM SuggestedAction sa
            WHERE sa.inspection.id = :inspectionId
              AND (:status IS NULL OR sa.status = :status)
              AND (:actionType IS NULL OR sa.actionType = :actionType)
            ORDER BY sa.createdAt DESC
            """)
    List<SuggestedAction> findByInspectionWithOptionalFilters(
            @Param("inspectionId") Long inspectionId,
            @Param("status") SuggestedActionStatus status,
            @Param("actionType") DecisionRuleActionType actionType);

    Optional<SuggestedAction> findByIdAndInspection_Id(Long id, Long inspectionId);

    @EntityGraph(attributePaths = {
            "inspection",
            "inspection.asset",
            "inspection.asset.department",
            "report",
            "ruleEvaluationResult"
    })
    Optional<SuggestedAction> findDetailedById(Long id);
}

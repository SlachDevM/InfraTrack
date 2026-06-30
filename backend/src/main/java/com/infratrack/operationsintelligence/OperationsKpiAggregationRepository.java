package com.infratrack.operationsintelligence;

import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.issue.IssueType;
import com.infratrack.preventivemaintenance.ExecutionCandidateStatus;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;
import com.infratrack.suggestedaction.SuggestedActionStatus;
import com.infratrack.workorder.WorkOrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OperationsKpiAggregationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public long countAssets(OperationsKpiScope scope) {
        return countWithDepartment("""
                SELECT COUNT(a) FROM Asset a
                WHERE (:departmentId IS NULL OR a.department.id = :departmentId)
                """, scope);
    }

    public Map<Long, Long> countAssetsByDepartment(OperationsKpiScope scope) {
        return countGroupedByLongKey("""
                SELECT a.department.id, COUNT(a) FROM Asset a
                WHERE (:departmentId IS NULL OR a.department.id = :departmentId)
                  AND a.department IS NOT NULL
                GROUP BY a.department.id
                """, scope);
    }

    public Map<Long, Long> countAssetsByCategory(OperationsKpiScope scope) {
        return countGroupedByLongKey("""
                SELECT a.assetCategory.id, COUNT(a) FROM Asset a
                WHERE (:departmentId IS NULL OR a.department.id = :departmentId)
                  AND a.assetCategory IS NOT NULL
                GROUP BY a.assetCategory.id
                """, scope);
    }

    public long countAssetsWithoutCategory(OperationsKpiScope scope) {
        return countWithDepartment("""
                SELECT COUNT(a) FROM Asset a
                WHERE a.assetCategory IS NULL
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                """, scope);
    }

    public long countAssetsWithoutDepartment(OperationsKpiScope scope) {
        return countWithDepartment("""
                SELECT COUNT(a) FROM Asset a
                WHERE a.department IS NULL
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                """, scope);
    }

    public long countInspectionsByStatus(OperationsKpiScope scope, InspectionStatus status) {
        return countWithDepartment("""
                SELECT COUNT(i) FROM Inspection i
                WHERE i.status = :status
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, scope, Map.of("status", status));
    }

    public long countOverdueInspections(OperationsKpiScope scope, LocalDate today) {
        return countWithDepartment("""
                SELECT COUNT(i) FROM Inspection i
                WHERE i.status = com.infratrack.inspection.InspectionStatus.ASSIGNED
                  AND i.expectedCompletionDate IS NOT NULL
                  AND i.expectedCompletionDate < :today
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, scope, Map.of("today", today));
    }

    public long countInProgressInspections(OperationsKpiScope scope, LocalDate today) {
        return countWithDepartment("""
                SELECT COUNT(i) FROM Inspection i
                WHERE i.status = com.infratrack.inspection.InspectionStatus.ASSIGNED
                  AND (i.expectedCompletionDate IS NULL OR i.expectedCompletionDate >= :today)
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, scope, Map.of("today", today));
    }

    public long countOpenIssues(OperationsKpiScope scope) {
        return countWithDepartment("""
                SELECT COUNT(i) FROM Issue i
                WHERE NOT EXISTS (SELECT 1 FROM OperationalDecision od WHERE od.issue.id = i.id)
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, scope);
    }

    public long countResolvedIssues(OperationsKpiScope scope) {
        return countWithDepartment("""
                SELECT COUNT(i) FROM Issue i
                WHERE EXISTS (SELECT 1 FROM OperationalDecision od WHERE od.issue.id = i.id)
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, scope);
    }

    public long countIssuesByType(OperationsKpiScope scope, IssueType issueType) {
        return countWithDepartment("""
                SELECT COUNT(i) FROM Issue i
                WHERE i.issueType = :issueType
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, scope, Map.of("issueType", issueType));
    }

    public Map<String, Long> countIssuesBySeverity(OperationsKpiScope scope) {
        return countGroupedByEnumKey("""
                SELECT i.severity, COUNT(i) FROM Issue i
                WHERE (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                GROUP BY i.severity
                """, scope, IssueSeverity.class);
    }

    public Map<String, Long> countIssuesByTypeGrouped(OperationsKpiScope scope) {
        return countGroupedByEnumKey("""
                SELECT i.issueType, COUNT(i) FROM Issue i
                WHERE (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                GROUP BY i.issueType
                """, scope, IssueType.class);
    }

    public long countWorkOrdersByStatus(OperationsKpiScope scope, WorkOrderStatus status) {
        return countWithDepartment("""
                SELECT COUNT(w) FROM WorkOrder w
                WHERE w.status = :status
                  AND (:departmentId IS NULL OR w.asset.department.id = :departmentId)
                """, scope, Map.of("status", status));
    }

    public long countPreventivePlansByStatus(OperationsKpiScope scope, PreventiveMaintenancePlanStatus status) {
        return countWithDepartment("""
                SELECT COUNT(p) FROM PreventiveMaintenancePlan p
                WHERE p.status = :status
                  AND (:departmentId IS NULL OR p.asset.department.id = :departmentId)
                """, scope, Map.of("status", status));
    }

    public long countExecutionCandidatesByStatus(OperationsKpiScope scope, ExecutionCandidateStatus status) {
        return countWithDepartment("""
                SELECT COUNT(c) FROM PreventiveExecutionCandidate c
                WHERE c.candidateStatus = :status
                  AND (:departmentId IS NULL OR c.asset.department.id = :departmentId)
                """, scope, Map.of("status", status));
    }

    public long countSchedulerRunsBetween(long startInclusive, long endExclusive) {
        return ((Number) entityManager.createQuery("""
                SELECT COUNT(r) FROM PreventiveSchedulerRun r
                WHERE r.startedAt >= :startInclusive AND r.startedAt < :endExclusive
                """)
                .setParameter("startInclusive", startInclusive)
                .setParameter("endExclusive", endExclusive)
                .getSingleResult()).longValue();
    }

    public long countRuleEvaluationReports(OperationsKpiScope scope) {
        return countWithDepartment("""
                SELECT COUNT(r) FROM RuleEvaluationReport r
                WHERE (:departmentId IS NULL OR r.inspection.asset.department.id = :departmentId)
                """, scope);
    }

    public long sumMatchedRuleResults(OperationsKpiScope scope) {
        Number result = (Number) applyDepartmentParams(entityManager.createQuery("""
                SELECT COALESCE(SUM(r.matchedCount), 0) FROM RuleEvaluationReport r
                WHERE (:departmentId IS NULL OR r.inspection.asset.department.id = :departmentId)
                """), scope)
                .getSingleResult();
        return result.longValue();
    }

    public long countSuggestedActionsByStatus(OperationsKpiScope scope, SuggestedActionStatus status) {
        return countWithDepartment("""
                SELECT COUNT(s) FROM SuggestedAction s
                WHERE s.status = :status
                  AND (:departmentId IS NULL OR s.inspection.asset.department.id = :departmentId)
                """, scope, Map.of("status", status));
    }

    private long countWithDepartment(String jpql, OperationsKpiScope scope) {
        return countWithDepartment(jpql, scope, Map.of());
    }

    private long countWithDepartment(String jpql, OperationsKpiScope scope, Map<String, Object> extraParams) {
        var query = applyDepartmentParams(entityManager.createQuery(jpql), scope);
        extraParams.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Map<Long, Long> countGroupedByLongKey(String jpql, OperationsKpiScope scope) {
        List<Object[]> rows = applyDepartmentParams(entityManager.createQuery(jpql, Object[].class), scope)
                .getResultList();
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    private <E extends Enum<E>> Map<String, Long> countGroupedByEnumKey(
            String jpql,
            OperationsKpiScope scope,
            Class<E> enumType) {
        List<Object[]> rows = applyDepartmentParams(entityManager.createQuery(jpql, Object[].class), scope)
                .getResultList();
        Map<String, Long> result = new HashMap<>();
        for (E constant : enumType.getEnumConstants()) {
            result.put(constant.name(), 0L);
        }
        for (Object[] row : rows) {
            E key = (E) row[0];
            result.put(key.name(), ((Number) row[1]).longValue());
        }
        return result;
    }

    private jakarta.persistence.Query applyDepartmentParams(
            jakarta.persistence.Query query,
            OperationsKpiScope scope) {
        return query.setParameter("departmentId", scope.departmentId());
    }
}

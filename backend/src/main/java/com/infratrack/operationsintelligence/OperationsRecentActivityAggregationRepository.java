package com.infratrack.operationsintelligence;

import com.infratrack.inspection.InspectionStatus;
import com.infratrack.preventivemaintenance.ExecutionCandidateStatus;
import com.infratrack.suggestedaction.SuggestedActionStatus;
import com.infratrack.workorder.WorkOrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OperationsRecentActivityAggregationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<RecentActivitySourceRow> findRecentActivityRows(OperationsKpiScope scope, int perSourceLimit) {
        List<RecentActivitySourceRow> rows = new ArrayList<>();
        rows.addAll(findCompletedInspections(scope, perSourceLimit));
        rows.addAll(findCreatedIssues(scope, perSourceLimit));
        rows.addAll(findCompletedWorkOrders(scope, perSourceLimit));
        rows.addAll(findGeneratedPreventiveCandidates(scope, perSourceLimit));
        rows.addAll(findApprovedPreventiveCandidates(scope, perSourceLimit));
        rows.addAll(findAcceptedSuggestedActions(scope, perSourceLimit));
        return rows;
    }

    @SuppressWarnings("unchecked")
    private List<RecentActivitySourceRow> findCompletedInspections(OperationsKpiScope scope, int limit) {
        List<Object[]> results = applyDepartmentParams(entityManager.createQuery("""
                SELECT a.id, a.name, i.completedAt
                FROM Inspection i
                JOIN i.asset a
                WHERE i.status = :status
                  AND i.completedAt IS NOT NULL
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                ORDER BY i.completedAt DESC
                """, Object[].class), scope)
                .setParameter("status", InspectionStatus.COMPLETED)
                .setMaxResults(limit)
                .getResultList();
        return mapLocalDateTimeRows(RecentActivityType.INSPECTION_COMPLETED, results);
    }

    @SuppressWarnings("unchecked")
    private List<RecentActivitySourceRow> findCreatedIssues(OperationsKpiScope scope, int limit) {
        List<Object[]> results = applyDepartmentParams(entityManager.createQuery("""
                SELECT a.id, a.name, i.recordedAt
                FROM Issue i
                JOIN i.asset a
                WHERE i.recordedAt IS NOT NULL
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                ORDER BY i.recordedAt DESC
                """, Object[].class), scope)
                .setMaxResults(limit)
                .getResultList();
        return mapLocalDateTimeRows(RecentActivityType.ISSUE_CREATED, results);
    }

    @SuppressWarnings("unchecked")
    private List<RecentActivitySourceRow> findCompletedWorkOrders(OperationsKpiScope scope, int limit) {
        List<Object[]> results = applyDepartmentParams(entityManager.createQuery("""
                SELECT a.id, a.name, w.updatedAt
                FROM WorkOrder w
                JOIN w.asset a
                WHERE w.status = :status
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                ORDER BY w.updatedAt DESC
                """, Object[].class), scope)
                .setParameter("status", WorkOrderStatus.COMPLETED)
                .setMaxResults(limit)
                .getResultList();
        return mapEpochMillisRows(RecentActivityType.WORK_ORDER_COMPLETED, results);
    }

    @SuppressWarnings("unchecked")
    private List<RecentActivitySourceRow> findGeneratedPreventiveCandidates(OperationsKpiScope scope, int limit) {
        List<Object[]> results = applyDepartmentParams(entityManager.createQuery("""
                SELECT a.id, a.name, c.createdAt
                FROM PreventiveExecutionCandidate c
                JOIN c.asset a
                WHERE c.createdAt IS NOT NULL
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                ORDER BY c.createdAt DESC
                """, Object[].class), scope)
                .setMaxResults(limit)
                .getResultList();
        return mapEpochMillisRows(RecentActivityType.PREVENTIVE_CANDIDATE_GENERATED, results);
    }

    @SuppressWarnings("unchecked")
    private List<RecentActivitySourceRow> findApprovedPreventiveCandidates(OperationsKpiScope scope, int limit) {
        List<Object[]> results = applyDepartmentParams(entityManager.createQuery("""
                SELECT a.id, a.name, c.decidedAt, c.planNameSnapshot
                FROM PreventiveExecutionCandidate c
                JOIN c.asset a
                WHERE c.candidateStatus = :status
                  AND c.decidedAt IS NOT NULL
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                ORDER BY c.decidedAt DESC
                """, Object[].class), scope)
                .setParameter("status", ExecutionCandidateStatus.APPROVED)
                .setMaxResults(limit)
                .getResultList();
        return mapEpochMillisRowsWithDescription(
                RecentActivityType.PREVENTIVE_CANDIDATE_APPROVED, results);
    }

    @SuppressWarnings("unchecked")
    private List<RecentActivitySourceRow> findAcceptedSuggestedActions(OperationsKpiScope scope, int limit) {
        List<Object[]> results = applyDepartmentParams(entityManager.createQuery("""
                SELECT a.id, a.name, s.decidedAt, s.title
                FROM SuggestedAction s
                JOIN s.inspection i
                JOIN i.asset a
                WHERE s.status = :status
                  AND s.decidedAt IS NOT NULL
                  AND (:departmentId IS NULL OR a.department.id = :departmentId)
                ORDER BY s.decidedAt DESC
                """, Object[].class), scope)
                .setParameter("status", SuggestedActionStatus.ACCEPTED)
                .setMaxResults(limit)
                .getResultList();
        return mapEpochMillisRowsWithDescription(RecentActivityType.SUGGESTED_ACTION_ACCEPTED, results);
    }

    private List<RecentActivitySourceRow> mapLocalDateTimeRows(RecentActivityType type, List<Object[]> results) {
        List<RecentActivitySourceRow> rows = new ArrayList<>(results.size());
        for (Object[] row : results) {
            Long assetId = (Long) row[0];
            String assetName = (String) row[1];
            LocalDateTime occurredAt = (LocalDateTime) row[2];
            Long occurredAtMillis = toEpochMillis(occurredAt);
            if (occurredAtMillis == null) {
                continue;
            }
            rows.add(new RecentActivitySourceRow(type, assetId, assetName, occurredAtMillis, assetName));
        }
        return rows;
    }

    private List<RecentActivitySourceRow> mapEpochMillisRows(RecentActivityType type, List<Object[]> results) {
        List<RecentActivitySourceRow> rows = new ArrayList<>(results.size());
        for (Object[] row : results) {
            Long assetId = (Long) row[0];
            String assetName = (String) row[1];
            Long occurredAt = (Long) row[2];
            if (occurredAt == null) {
                continue;
            }
            rows.add(new RecentActivitySourceRow(type, assetId, assetName, occurredAt, assetName));
        }
        return rows;
    }

    private List<RecentActivitySourceRow> mapEpochMillisRowsWithDescription(
            RecentActivityType type,
            List<Object[]> results) {
        List<RecentActivitySourceRow> rows = new ArrayList<>(results.size());
        for (Object[] row : results) {
            Long assetId = (Long) row[0];
            String assetName = (String) row[1];
            Long occurredAt = (Long) row[2];
            String description = (String) row[3];
            if (occurredAt == null) {
                continue;
            }
            String resolvedDescription = description != null && !description.isBlank() ? description : assetName;
            rows.add(new RecentActivitySourceRow(type, assetId, assetName, occurredAt, resolvedDescription));
        }
        return rows;
    }

    private static Long toEpochMillis(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private jakarta.persistence.TypedQuery<Object[]> applyDepartmentParams(
            jakarta.persistence.TypedQuery<Object[]> query,
            OperationsKpiScope scope) {
        return query.setParameter("departmentId", scope.departmentId());
    }
}

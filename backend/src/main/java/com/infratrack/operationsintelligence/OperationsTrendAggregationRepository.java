package com.infratrack.operationsintelligence;

import com.infratrack.inspection.InspectionStatus;
import com.infratrack.suggestedaction.SuggestedActionStatus;
import com.infratrack.workorder.WorkOrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class OperationsTrendAggregationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<LocalDateTime> findCompletedInspectionTimestamps(
            OperationsKpiScope scope,
            long fromInclusive,
            long toExclusive) {
        LocalDateTime from = toLocalDateTime(fromInclusive);
        LocalDateTime to = toLocalDateTime(toExclusive);
        return applyDepartmentParams(entityManager.createQuery("""
                SELECT i.completedAt FROM Inspection i
                WHERE i.status = :status
                  AND i.completedAt IS NOT NULL
                  AND i.completedAt >= :from
                  AND i.completedAt < :to
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, LocalDateTime.class), scope)
                .setParameter("status", InspectionStatus.COMPLETED)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<LocalDateTime> findIssueCreatedTimestamps(
            OperationsKpiScope scope,
            long fromInclusive,
            long toExclusive) {
        LocalDateTime from = toLocalDateTime(fromInclusive);
        LocalDateTime to = toLocalDateTime(toExclusive);
        return applyDepartmentParams(entityManager.createQuery("""
                SELECT i.recordedAt FROM Issue i
                WHERE i.recordedAt IS NOT NULL
                  AND i.recordedAt >= :from
                  AND i.recordedAt < :to
                  AND (:departmentId IS NULL OR i.asset.department.id = :departmentId)
                """, LocalDateTime.class), scope)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    /**
     * Work orders have no dedicated completion timestamp; {@code updatedAt} is used when status is COMPLETED.
     */
    @SuppressWarnings("unchecked")
    public List<Long> findCompletedWorkOrderTimestamps(
            OperationsKpiScope scope,
            long fromInclusive,
            long toExclusive) {
        return applyDepartmentParams(entityManager.createQuery("""
                SELECT w.updatedAt FROM WorkOrder w
                WHERE w.status = :status
                  AND w.updatedAt >= :from
                  AND w.updatedAt < :to
                  AND (:departmentId IS NULL OR w.asset.department.id = :departmentId)
                """), scope)
                .setParameter("status", WorkOrderStatus.COMPLETED)
                .setParameter("from", fromInclusive)
                .setParameter("to", toExclusive)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findPreventiveCandidateGeneratedTimestamps(
            OperationsKpiScope scope,
            long fromInclusive,
            long toExclusive) {
        return applyDepartmentParams(entityManager.createQuery("""
                SELECT c.createdAt FROM PreventiveExecutionCandidate c
                WHERE c.createdAt >= :from
                  AND c.createdAt < :to
                  AND (:departmentId IS NULL OR c.asset.department.id = :departmentId)
                """), scope)
                .setParameter("from", fromInclusive)
                .setParameter("to", toExclusive)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findAcceptedSuggestedActionTimestamps(
            OperationsKpiScope scope,
            long fromInclusive,
            long toExclusive) {
        return applyDepartmentParams(entityManager.createQuery("""
                SELECT s.decidedAt FROM SuggestedAction s
                WHERE s.status = :status
                  AND s.decidedAt IS NOT NULL
                  AND s.decidedAt >= :from
                  AND s.decidedAt < :to
                  AND (:departmentId IS NULL OR s.inspection.asset.department.id = :departmentId)
                """), scope)
                .setParameter("status", SuggestedActionStatus.ACCEPTED)
                .setParameter("from", fromInclusive)
                .setParameter("to", toExclusive)
                .getResultList();
    }

    private static LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    private jakarta.persistence.Query applyDepartmentParams(
            jakarta.persistence.Query query,
            OperationsKpiScope scope) {
        return query.setParameter("departmentId", scope.departmentId());
    }
}

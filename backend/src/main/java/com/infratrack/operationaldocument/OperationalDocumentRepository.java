package com.infratrack.operationaldocument;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OperationalDocumentRepository extends JpaRepository<OperationalDocument, Long> {

    @EntityGraph(attributePaths = {"asset"})
    List<OperationalDocument> findByAssetIdOrderByUploadedAtDesc(Long assetId);

    @EntityGraph(attributePaths = {"asset"})
    Page<OperationalDocument> findByAssetIdOrderByUploadedAtDesc(Long assetId, Pageable pageable);

    @EntityGraph(attributePaths = {"asset"})
    List<OperationalDocument> findByAssetIdAndOwnerTypeOrderByUploadedAtDesc(
            Long assetId,
            OperationalDocumentOwnerType ownerType);

    @EntityGraph(attributePaths = {"asset"})
    List<OperationalDocument> findByAssetIdInAndOwnerTypeOrderByUploadedAtDesc(
            Collection<Long> assetIds,
            OperationalDocumentOwnerType ownerType);

    @EntityGraph(attributePaths = {"asset"})
    @Query("""
            SELECT d FROM OperationalDocument d
            WHERE d.asset.id = :assetId
              AND (
                (d.ownerType = com.infratrack.operationaldocument.OperationalDocumentOwnerType.INSPECTION
                  AND EXISTS (
                    SELECT 1 FROM Inspection i
                    WHERE i.id = d.ownerId
                      AND (i.assignedToUserId = :userId OR i.completedByUserId = :userId)))
                OR (d.ownerType = com.infratrack.operationaldocument.OperationalDocumentOwnerType.ISSUE
                  AND EXISTS (
                    SELECT 1 FROM Issue iss
                    WHERE iss.id = d.ownerId AND iss.recordedByUserId = :userId))
                OR (d.ownerType = com.infratrack.operationaldocument.OperationalDocumentOwnerType.WORK_ORDER
                  AND EXISTS (
                    SELECT 1 FROM WorkOrder wo
                    WHERE wo.id = d.ownerId AND wo.assignedToUserId = :userId))
                OR (d.ownerType = com.infratrack.operationaldocument.OperationalDocumentOwnerType.MAINTENANCE_ACTIVITY
                  AND EXISTS (
                    SELECT 1 FROM MaintenanceActivity ma
                    WHERE ma.id = d.ownerId AND ma.performedByUserId = :userId))
              )
            """)
    Page<OperationalDocument> findVisibleToFieldUser(
            @Param("assetId") Long assetId,
            @Param("userId") Long userId,
            Pageable pageable);
}

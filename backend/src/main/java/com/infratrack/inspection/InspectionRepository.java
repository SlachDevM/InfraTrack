package com.infratrack.inspection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface InspectionRepository extends JpaRepository<Inspection, Long>, InspectionRepositoryCustom {

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    List<Inspection> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    Page<Inspection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByBusinessTriggerIdAndStatus(Long businessTriggerId, InspectionStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.department", "businessTrigger"})
    @Query("""
            SELECT i FROM Inspection i
            WHERE i.status = :status
              AND i.issueIdentified = true
              AND i.completedByUserId = :userId
              AND i.asset.department.id = :departmentId
              AND NOT EXISTS (SELECT 1 FROM Issue issue WHERE issue.inspection.id = i.id)
            """)
    Page<Inspection> findEligibleForIssueRecording(
            @Param("status") InspectionStatus status,
            @Param("userId") Long userId,
            @Param("departmentId") Long departmentId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"asset", "businessTrigger"})
    List<Inspection> findAllByAsset_IdOrderByCompletedAtDesc(Long assetId);

    @EntityGraph(attributePaths = {"asset"})
    Optional<Inspection> findFirstByAsset_IdAndStatusOrderByCompletedAtDesc(
            Long assetId,
            InspectionStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAsset_IdAndStatus(Long assetId, InspectionStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAsset_IdInAndStatus(Collection<Long> assetIds, InspectionStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.department", "inspectionTemplate"})
    Optional<Inspection> findWithEvaluationContextById(Long id);

    @EntityGraph(attributePaths = {
            "asset", "asset.department", "asset.assetCategory", "inspectionTemplate"})
    Optional<Inspection> findMobileBundleById(Long id);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAssignedToUserId(Long assignedToUserId);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(
            Long assignedToUserId,
            Long updatedAt);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAssignedToUserIdAndUpdatedAtLessThanEqual(Long assignedToUserId, Long updatedAt);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAssignedToUserIdAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
            Long assignedToUserId,
            Long updatedAtSince,
            Long updatedAtUntil);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAsset_Department_Id(Long departmentId);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAsset_Department_IdAndStatus(Long departmentId, InspectionStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqual(
            Long departmentId,
            InspectionStatus status,
            Long updatedAt);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAsset_Department_IdAndStatusAndUpdatedAtLessThanEqual(
            Long departmentId,
            InspectionStatus status,
            Long updatedAt);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
            Long departmentId,
            InspectionStatus status,
            Long updatedAtSince,
            Long updatedAtUntil);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByStatus(InspectionStatus status);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByStatusAndUpdatedAtGreaterThanEqual(InspectionStatus status, Long updatedAt);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByStatusAndUpdatedAtLessThanEqual(InspectionStatus status, Long updatedAt);

    @EntityGraph(attributePaths = {"asset", "asset.assetCategory", "inspectionTemplate"})
    List<Inspection> findByStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
            InspectionStatus status,
            Long updatedAtSince,
            Long updatedAtUntil);

    long countByAssignedToUserIdAndStatus(Long assignedToUserId, InspectionStatus status);

    @Query("""
            SELECT COUNT(i) FROM Inspection i
            WHERE i.assignedToUserId = :userId
              AND i.status = :status
              AND i.expectedCompletionDate IS NOT NULL
              AND i.expectedCompletionDate < :today
            """)
    long countOverdueByAssignedUser(
            @Param("userId") Long userId,
            @Param("status") InspectionStatus status,
            @Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(i) FROM Inspection i
            WHERE i.completedByUserId = :userId
              AND i.completedAt >= :start
              AND i.completedAt < :end
            """)
    long countCompletedByUserBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

}

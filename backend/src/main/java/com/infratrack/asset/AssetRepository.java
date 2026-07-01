package com.infratrack.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Override
    @EntityGraph(attributePaths = {"department", "assetCategory"})
    Optional<Asset> findById(Long id);

    @EntityGraph(attributePaths = {"department", "assetCategory"})
    List<Asset> findAllByOrderByRegistrationDateDesc();

    @EntityGraph(attributePaths = {"department", "assetCategory"})
    Page<Asset> findAllByOrderByRegistrationDateDesc(Pageable pageable);

    boolean existsByNameIgnoreCaseAndDepartmentIdAndAssetCategoryId(
            String name,
            Long departmentId,
            Long assetCategoryId);

    boolean existsByDepartmentId(Long departmentId);

    @EntityGraph(attributePaths = {"department", "assetCategory"})
    Page<Asset> findAllByDepartment_IdOrderByRegistrationDateDesc(Long departmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"department", "assetCategory"})
    @Query("""
            SELECT a FROM Asset a
            WHERE (
              (:userDepartmentId IS NOT NULL AND a.department.id = :userDepartmentId)
              OR EXISTS (
                SELECT 1 FROM DelegatedAuthority d
                WHERE d.delegateManagerUserId = :userId
                  AND d.targetDepartment.id = a.department.id
                  AND d.revoked = false
                  AND d.validFrom <= :at
                  AND d.validUntil > :at
              )
            )
            """)
    Page<Asset> findEligibleForOperationalDocumentUpload(
            @Param("userId") Long userId,
            @Param("userDepartmentId") Long userDepartmentId,
            @Param("at") LocalDateTime at,
            Pageable pageable);

    @EntityGraph(attributePaths = {"department", "assetCategory"})
    @Query("""
            SELECT a FROM Asset a
            WHERE (:departmentId IS NULL OR a.department.id = :departmentId)
              AND (:from IS NULL OR a.createdAt >= :from)
              AND (:to IS NULL OR a.createdAt <= :to)
            ORDER BY a.createdAt DESC
            """)
    List<Asset> findForExport(
            @Param("departmentId") Long departmentId,
            @Param("from") Long from,
            @Param("to") Long to);
}

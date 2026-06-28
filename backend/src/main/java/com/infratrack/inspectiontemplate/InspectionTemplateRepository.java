package com.infratrack.inspectiontemplate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InspectionTemplateRepository extends JpaRepository<InspectionTemplate, Long> {

    @EntityGraph(attributePaths = {"assetCategory"})
    @Query("""
            SELECT t FROM InspectionTemplate t
            WHERE (:assetCategoryId IS NULL OR t.assetCategory.id = :assetCategoryId)
              AND (:status IS NULL OR t.status = :status)
            """)
    Page<InspectionTemplate> findFiltered(
            @Param("assetCategoryId") Long assetCategoryId,
            @Param("status") InspectionTemplateStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"assetCategory"})
    Optional<InspectionTemplate> findDetailedById(Long id);
}

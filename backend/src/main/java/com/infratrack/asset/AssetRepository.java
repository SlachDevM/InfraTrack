package com.infratrack.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    @EntityGraph(attributePaths = {"department", "assetCategory"})
    List<Asset> findAllByOrderByRegistrationDateDesc();

    @EntityGraph(attributePaths = {"department", "assetCategory"})
    Page<Asset> findAllByOrderByRegistrationDateDesc(Pageable pageable);

    boolean existsByNameIgnoreCaseAndDepartmentIdAndAssetCategoryId(
            String name,
            Long departmentId,
            Long assetCategoryId);

    boolean existsByDepartmentId(Long departmentId);
}

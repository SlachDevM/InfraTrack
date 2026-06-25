package com.infratrack.asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCaseAndDepartmentIdAndAssetCategoryId(
            String name,
            Long departmentId,
            Long assetCategoryId);
}

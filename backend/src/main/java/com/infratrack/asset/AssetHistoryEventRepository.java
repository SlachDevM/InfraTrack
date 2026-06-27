package com.infratrack.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHistoryEventRepository extends JpaRepository<AssetHistoryEvent, Long> {

    List<AssetHistoryEvent> findByAssetIdOrderByEventDateDescCreatedAtDesc(Long assetId);

    Page<AssetHistoryEvent> findByAssetIdOrderByEventDateDescCreatedAtDesc(Long assetId, Pageable pageable);
}

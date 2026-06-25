package com.infratrack.asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHistoryEventRepository extends JpaRepository<AssetHistoryEvent, Long> {

    List<AssetHistoryEvent> findByAssetIdOrderByCreatedAtAsc(Long assetId);
}

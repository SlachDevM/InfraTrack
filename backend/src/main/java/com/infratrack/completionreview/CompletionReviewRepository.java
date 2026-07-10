package com.infratrack.completionreview;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompletionReviewRepository extends JpaRepository<CompletionReview, Long> {

    boolean existsByMaintenanceActivityId(Long maintenanceActivityId);

    Optional<CompletionReview> findByMaintenanceActivityId(Long maintenanceActivityId);

    List<CompletionReview> findByMaintenanceActivityIdIn(Collection<Long> maintenanceActivityIds);

    @EntityGraph(attributePaths = {"asset", "maintenanceActivity"})
    List<CompletionReview> findAllByAsset_IdOrderByReviewedAtDesc(Long assetId);
}

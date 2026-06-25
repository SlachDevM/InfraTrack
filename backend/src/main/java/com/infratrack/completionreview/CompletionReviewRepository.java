package com.infratrack.completionreview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompletionReviewRepository extends JpaRepository<CompletionReview, Long> {

    boolean existsByMaintenanceActivityId(Long maintenanceActivityId);

    Optional<CompletionReview> findByMaintenanceActivityId(Long maintenanceActivityId);
}

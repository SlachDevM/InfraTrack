package com.infratrack.workorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    List<WorkOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    Page<WorkOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"asset", "asset.department", "operationalDecision"})
    Optional<WorkOrder> findDetailedById(Long id);

    boolean existsByOperationalDecisionId(Long operationalDecisionId);
}

package com.infratrack.workorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    @EntityGraph(attributePaths = {"asset", "operationalDecision"})
    List<WorkOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"asset", "operationalDecision"})
    Page<WorkOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByOperationalDecisionId(Long operationalDecisionId);
}

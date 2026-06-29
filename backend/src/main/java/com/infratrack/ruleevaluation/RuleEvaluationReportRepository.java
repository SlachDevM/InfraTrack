package com.infratrack.ruleevaluation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RuleEvaluationReportRepository extends JpaRepository<RuleEvaluationReport, Long> {

    List<RuleEvaluationReport> findByInspection_IdOrderByEvaluatedAtDesc(Long inspectionId);

    @EntityGraph(attributePaths = {"results", "inspection"})
    Optional<RuleEvaluationReport> findFirstByInspection_IdOrderByEvaluatedAtDesc(Long inspectionId);

    @EntityGraph(attributePaths = {"results", "inspection"})
    Optional<RuleEvaluationReport> findByIdAndInspection_Id(Long id, Long inspectionId);
}

package com.infratrack.operationaldecision;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationalDecisionRepository extends JpaRepository<OperationalDecision, Long> {

    List<OperationalDecision> findAllByOrderByCreatedAtDesc();

    boolean existsByIssueId(Long issueId);
}

package com.infratrack.operationaldecision;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationalDecisionRepository extends JpaRepository<OperationalDecision, Long> {

    List<OperationalDecision> findAllByOrderByCreatedAtDesc();

    Page<OperationalDecision> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByIssueId(Long issueId);
}

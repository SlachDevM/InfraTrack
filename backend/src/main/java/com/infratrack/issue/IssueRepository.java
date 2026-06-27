package com.infratrack.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findAllByOrderByCreatedAtDesc();

    Page<Issue> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByInspectionId(Long inspectionId);
}

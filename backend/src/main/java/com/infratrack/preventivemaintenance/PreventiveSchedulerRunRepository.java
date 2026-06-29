package com.infratrack.preventivemaintenance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreventiveSchedulerRunRepository extends JpaRepository<PreventiveSchedulerRun, Long> {

    Page<PreventiveSchedulerRun> findAllByOrderByStartedAtDesc(Pageable pageable);
}

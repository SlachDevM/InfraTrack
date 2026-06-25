package com.infratrack.maintenanceactivity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceActivityRepository extends JpaRepository<MaintenanceActivity, Long> {

    boolean existsByWorkOrderId(Long workOrderId);

    List<MaintenanceActivity> findAllByOrderByCompletedAtDesc();
}

package com.infratrack.maintenanceactivity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceActivityRepository extends JpaRepository<MaintenanceActivity, Long> {

    boolean existsByWorkOrderId(Long workOrderId);
}

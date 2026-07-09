package com.infratrack.businesstrigger;

import com.infratrack.inspection.InspectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusinessTriggerRepository extends JpaRepository<BusinessTrigger, Long> {

    List<BusinessTrigger> findAllByOrderByCreatedAtDesc();

    Page<BusinessTrigger> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT bt FROM BusinessTrigger bt
            WHERE NOT EXISTS (
                SELECT 1 FROM Inspection i
                WHERE i.businessTrigger = bt AND i.status = :assignedStatus
            )
            ORDER BY bt.createdAt DESC
            """)
    Page<BusinessTrigger> findEligibleForInspectionOrderByCreatedAtDesc(
            @Param("assignedStatus") InspectionStatus assignedStatus,
            Pageable pageable);
}

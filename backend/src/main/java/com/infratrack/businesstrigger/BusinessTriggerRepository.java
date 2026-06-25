package com.infratrack.businesstrigger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessTriggerRepository extends JpaRepository<BusinessTrigger, Long> {

    List<BusinessTrigger> findAllByOrderByCreatedAtDesc();
}

package com.infratrack.operationsintelligence.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardPreferencesRepository extends JpaRepository<DashboardPreferences, Long> {

    Optional<DashboardPreferences> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}

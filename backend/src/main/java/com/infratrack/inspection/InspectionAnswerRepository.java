package com.infratrack.inspection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InspectionAnswerRepository extends JpaRepository<InspectionAnswer, Long> {

    @Query("""
            SELECT a FROM InspectionAnswer a
            JOIN FETCH a.question q
            WHERE a.inspection.id = :inspectionId
            ORDER BY q.displayOrder ASC
            """)
    List<InspectionAnswer> findByInspectionIdOrderByQuestionDisplayOrder(@Param("inspectionId") Long inspectionId);

    boolean existsByInspectionIdAndQuestionId(Long inspectionId, Long questionId);

    Optional<InspectionAnswer> findByInspectionIdAndQuestionId(Long inspectionId, Long questionId);
}

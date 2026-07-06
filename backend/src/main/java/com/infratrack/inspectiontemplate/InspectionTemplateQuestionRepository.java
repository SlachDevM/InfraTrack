package com.infratrack.inspectiontemplate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionTemplateQuestionRepository extends JpaRepository<InspectionTemplateQuestion, Long> {

    List<InspectionTemplateQuestion> findByInspectionTemplateIdOrderByDisplayOrderAsc(Long inspectionTemplateId);

    List<InspectionTemplateQuestion> findByInspectionTemplateIdInOrderByInspectionTemplateIdAscDisplayOrderAsc(
            Iterable<Long> inspectionTemplateIds);

    Optional<InspectionTemplateQuestion> findByIdAndInspectionTemplateId(Long id, Long inspectionTemplateId);

    boolean existsByInspectionTemplateIdAndCode(Long inspectionTemplateId, String code);

    long countByInspectionTemplateIdAndActiveTrue(Long inspectionTemplateId);
}

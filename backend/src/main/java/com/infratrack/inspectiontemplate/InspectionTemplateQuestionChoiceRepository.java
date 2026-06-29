package com.infratrack.inspectiontemplate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InspectionTemplateQuestionChoiceRepository
        extends JpaRepository<InspectionTemplateQuestionChoice, Long> {

    List<InspectionTemplateQuestionChoice> findByQuestionIdOrderByDisplayOrderAsc(Long questionId);

    List<InspectionTemplateQuestionChoice> findByQuestionIdInOrderByQuestionIdAscDisplayOrderAsc(
            Collection<Long> questionIds);

    Optional<InspectionTemplateQuestionChoice> findByIdAndQuestionId(Long id, Long questionId);

    Optional<InspectionTemplateQuestionChoice> findByQuestionIdAndCode(Long questionId, String code);

    boolean existsByQuestionIdAndCode(Long questionId, String code);
}

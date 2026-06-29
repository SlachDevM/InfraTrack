package com.infratrack.inspectiontemplate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionTemplateQuestionRuleRepository
        extends JpaRepository<InspectionTemplateQuestionRule, Long> {

    List<InspectionTemplateQuestionRule> findByQuestionIdOrderByPriorityAscRuleCodeAsc(Long questionId);

    List<InspectionTemplateQuestionRule> findByQuestionIdAndActiveTrueOrderByPriorityAscRuleCodeAsc(Long questionId);

    Optional<InspectionTemplateQuestionRule> findByIdAndQuestionId(Long id, Long questionId);

    boolean existsByQuestionIdAndRuleCode(Long questionId, String ruleCode);
}

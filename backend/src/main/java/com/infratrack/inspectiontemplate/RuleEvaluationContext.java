package com.infratrack.inspectiontemplate;

import com.infratrack.asset.Asset;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;

/**
 * Surrounding data for decision rule evaluation (V2 Domain Engine A3.2.1).
 * Evaluation remains answer-based; context prepares future asset, department, and template rules.
 */
public final class RuleEvaluationContext {

    private final Inspection inspection;
    private final Asset asset;
    private final Department department;
    private final InspectionTemplate template;
    private final InspectionTemplateQuestion question;
    private final InspectionAnswer answer;

    private RuleEvaluationContext(
            Inspection inspection,
            Asset asset,
            Department department,
            InspectionTemplate template,
            InspectionTemplateQuestion question,
            InspectionAnswer answer) {
        this.inspection = inspection;
        this.asset = asset;
        this.department = department;
        this.template = template;
        this.question = question;
        this.answer = answer;
    }

    public static RuleEvaluationContext from(Inspection inspection, InspectionAnswer answer) {
        Asset asset = inspection != null ? inspection.getAsset() : null;
        Department department = asset != null ? asset.getDepartment() : null;
        InspectionTemplate template = inspection != null ? inspection.getInspectionTemplate() : null;
        InspectionTemplateQuestion question = answer != null ? answer.getQuestion() : null;
        return new RuleEvaluationContext(inspection, asset, department, template, question, answer);
    }

    public Inspection getInspection() {
        return inspection;
    }

    public Asset getAsset() {
        return asset;
    }

    public Department getDepartment() {
        return department;
    }

    public InspectionTemplate getTemplate() {
        return template;
    }

    public InspectionTemplateQuestion getQuestion() {
        return question;
    }

    public InspectionAnswer getAnswer() {
        return answer;
    }
}

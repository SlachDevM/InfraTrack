package com.infratrack.inspectiontemplate;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection_template_questions")
public class InspectionTemplateQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_template_id", nullable = false)
    private InspectionTemplate inspectionTemplate;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    @Column(name = "help_text", length = 4000)
    private String helpText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private InspectionTemplateQuestionType questionType;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected InspectionTemplateQuestion() {
    }

    public InspectionTemplateQuestion(
            InspectionTemplate inspectionTemplate,
            String questionText,
            String helpText,
            InspectionTemplateQuestionType questionType,
            boolean required,
            Integer displayOrder) {
        this.inspectionTemplate = inspectionTemplate;
        this.questionText = questionText;
        this.helpText = helpText;
        this.questionType = questionType;
        this.required = required;
        this.displayOrder = displayOrder;
        this.active = true;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InspectionTemplate getInspectionTemplate() {
        return inspectionTemplate;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public InspectionTemplateQuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(InspectionTemplateQuestionType questionType) {
        this.questionType = questionType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = System.currentTimeMillis();
    }
}

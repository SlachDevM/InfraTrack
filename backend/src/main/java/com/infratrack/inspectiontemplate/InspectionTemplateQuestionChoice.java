package com.infratrack.inspectiontemplate;

import jakarta.persistence.*;

@Entity
@Table(
        name = "inspection_template_question_choices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inspection_template_question_choices_question_code",
                columnNames = {"question_id", "code"}
        )
)
public class InspectionTemplateQuestionChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InspectionTemplateQuestion question;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 500)
    private String label;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected InspectionTemplateQuestionChoice() {
    }

    public InspectionTemplateQuestionChoice(
            InspectionTemplateQuestion question,
            String code,
            String label,
            Integer displayOrder) {
        this.question = question;
        this.code = code;
        this.label = label;
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

    public InspectionTemplateQuestion getQuestion() {
        return question;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

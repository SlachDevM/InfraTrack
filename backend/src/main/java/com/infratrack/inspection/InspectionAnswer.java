package com.infratrack.inspection;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "inspection_answers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inspection_answers_inspection_question",
                columnNames = {"inspection_id", "question_id"}
        )
)
public class InspectionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InspectionTemplateQuestion question;

    @Column(name = "question_code_snapshot", nullable = false, length = 100)
    private String questionCodeSnapshot;

    @Column(name = "question_text_snapshot", nullable = false, length = 2000)
    private String questionTextSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type_snapshot", nullable = false)
    private InspectionAnswerQuestionTypeSnapshot questionTypeSnapshot;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "text_value", length = 4000)
    private String textValue;

    @Column(name = "number_value", precision = 19, scale = 4)
    private BigDecimal numberValue;

    @Column(name = "choice_code_value", length = 100)
    private String choiceCodeValue;

    @Column(name = "choice_label_snapshot", length = 500)
    private String choiceLabelSnapshot;

    @Column(name = "number_unit_snapshot", length = 50)
    private String numberUnitSnapshot;

    @Column(name = "number_min_snapshot", precision = 19, scale = 4)
    private BigDecimal numberMinSnapshot;

    @Column(name = "number_max_snapshot", precision = 19, scale = 4)
    private BigDecimal numberMaxSnapshot;

    @Column(name = "decimal_places_snapshot")
    private Integer decimalPlacesSnapshot;

    @Column(name = "unit_code_snapshot", length = 50)
    private String unitCodeSnapshot;

    @Column(name = "unit_symbol_snapshot", length = 20)
    private String unitSymbolSnapshot;

    @Column(name = "unit_name_snapshot", length = 100)
    private String unitNameSnapshot;

    @Column(name = "question_version_snapshot")
    private Integer questionVersionSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected InspectionAnswer() {
    }

    public InspectionAnswer(
            Inspection inspection,
            InspectionTemplateQuestion question,
            String questionCodeSnapshot,
            String questionTextSnapshot,
            InspectionAnswerQuestionTypeSnapshot questionTypeSnapshot,
            Boolean booleanValue,
            String textValue,
            BigDecimal numberValue,
            String choiceCodeValue,
            String choiceLabelSnapshot,
            String numberUnitSnapshot,
            BigDecimal numberMinSnapshot,
            BigDecimal numberMaxSnapshot,
            Integer decimalPlacesSnapshot,
            String unitCodeSnapshot,
            String unitSymbolSnapshot,
            String unitNameSnapshot,
            Integer questionVersionSnapshot) {
        this.inspection = inspection;
        this.question = question;
        this.questionCodeSnapshot = questionCodeSnapshot;
        this.questionTextSnapshot = questionTextSnapshot;
        this.questionTypeSnapshot = questionTypeSnapshot;
        this.booleanValue = booleanValue;
        this.textValue = textValue;
        this.numberValue = numberValue;
        this.choiceCodeValue = choiceCodeValue;
        this.choiceLabelSnapshot = choiceLabelSnapshot;
        this.numberUnitSnapshot = numberUnitSnapshot;
        this.numberMinSnapshot = numberMinSnapshot;
        this.numberMaxSnapshot = numberMaxSnapshot;
        this.decimalPlacesSnapshot = decimalPlacesSnapshot;
        this.unitCodeSnapshot = unitCodeSnapshot;
        this.unitSymbolSnapshot = unitSymbolSnapshot;
        this.unitNameSnapshot = unitNameSnapshot;
        this.questionVersionSnapshot = questionVersionSnapshot;
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

    public Inspection getInspection() {
        return inspection;
    }

    public InspectionTemplateQuestion getQuestion() {
        return question;
    }

    public String getQuestionCodeSnapshot() {
        return questionCodeSnapshot;
    }

    public String getQuestionTextSnapshot() {
        return questionTextSnapshot;
    }

    public InspectionAnswerQuestionTypeSnapshot getQuestionTypeSnapshot() {
        return questionTypeSnapshot;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public BigDecimal getNumberValue() {
        return numberValue;
    }

    public String getChoiceCodeValue() {
        return choiceCodeValue;
    }

    public String getChoiceLabelSnapshot() {
        return choiceLabelSnapshot;
    }

    public String getNumberUnitSnapshot() {
        return numberUnitSnapshot;
    }

    public BigDecimal getNumberMinSnapshot() {
        return numberMinSnapshot;
    }

    public BigDecimal getNumberMaxSnapshot() {
        return numberMaxSnapshot;
    }

    public Integer getDecimalPlacesSnapshot() {
        return decimalPlacesSnapshot;
    }

    public String getUnitCodeSnapshot() {
        return unitCodeSnapshot;
    }

    public String getUnitSymbolSnapshot() {
        return unitSymbolSnapshot;
    }

    public String getUnitNameSnapshot() {
        return unitNameSnapshot;
    }

    public Integer getQuestionVersionSnapshot() {
        return questionVersionSnapshot;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    void applyValuesFrom(InspectionAnswer source) {
        this.questionCodeSnapshot = source.getQuestionCodeSnapshot();
        this.questionTextSnapshot = source.getQuestionTextSnapshot();
        this.questionTypeSnapshot = source.getQuestionTypeSnapshot();
        this.booleanValue = source.getBooleanValue();
        this.textValue = source.getTextValue();
        this.numberValue = source.getNumberValue();
        this.choiceCodeValue = source.getChoiceCodeValue();
        this.choiceLabelSnapshot = source.getChoiceLabelSnapshot();
        this.numberUnitSnapshot = source.getNumberUnitSnapshot();
        this.numberMinSnapshot = source.getNumberMinSnapshot();
        this.numberMaxSnapshot = source.getNumberMaxSnapshot();
        this.decimalPlacesSnapshot = source.getDecimalPlacesSnapshot();
        this.unitCodeSnapshot = source.getUnitCodeSnapshot();
        this.unitSymbolSnapshot = source.getUnitSymbolSnapshot();
        this.unitNameSnapshot = source.getUnitNameSnapshot();
        this.questionVersionSnapshot = source.getQuestionVersionSnapshot();
        this.updatedAt = System.currentTimeMillis();
    }
}

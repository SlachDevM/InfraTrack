package com.infratrack.mobile.sync;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.mobile.MobileInspectionChecklistLoader;
import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.sync.dto.SyncInspectionChoiceDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionQuestionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionTemplateDeltaResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectionSyncDeltaServiceTest {

    private static final long TEMPLATE_ID = 50L;

    @Mock
    private MobileService mobileService;

    @Mock
    private InspectionAnswerRepository inspectionAnswerRepository;

    @Mock
    private UserNameLookup userNameLookup;

    @Mock
    private MobileInspectionChecklistLoader checklistLoader;

    private InspectionSyncDeltaService deltaService;

    @BeforeEach
    void setUp() {
        deltaService = new InspectionSyncDeltaService(
                mobileService,
                inspectionAnswerRepository,
                userNameLookup,
                checklistLoader);
        when(checklistLoader.loadChecklistPayloadByTemplateIds(any()))
                .thenReturn(MobileInspectionChecklistLoader.ChecklistPayload.empty());
    }

    @Test
    void build_nullSyncToken_returnsFullInspectionDelta() {
        Inspection inspection = inspection(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        when(mobileService.listScopedInspectionsForSync(fieldUser, null)).thenReturn(List.of(inspection));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L)))
                .thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));

        var result = deltaService.build(fieldUser, null);

        assertThat(result.warnings()).isEmpty();
        assertThat(result.delta().getInspections()).hasSize(1);
        assertThat(result.delta().getInspections().get(0).getId()).isEqualTo(100L);
        assertThat(result.delta().getInspections().get(0).getAssignedToName()).isEqualTo("Field User");
        assertThat(result.delta().getAssets()).isEmpty();
        assertThat(result.delta().getWorkOrders()).isEmpty();
        verify(mobileService).listScopedInspectionsForSync(fieldUser, null);
    }

    @Test
    void build_validSyncToken_queriesInspectionsUpdatedSinceToken() {
        Inspection changed = inspection(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        long sinceMillis = java.time.Instant.parse("2026-07-05T08:00:00Z").toEpochMilli();
        when(mobileService.listScopedInspectionsForSync(fieldUser, sinceMillis)).thenReturn(List.of(changed));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L)))
                .thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));

        String syncToken = SyncToken.issue(java.time.Instant.parse("2026-07-05T08:00:00Z")).toOpaqueValue();
        var result = deltaService.build(fieldUser, syncToken);

        assertThat(result.warnings()).isEmpty();
        assertThat(result.delta().getInspections()).hasSize(1);
        verify(mobileService).listScopedInspectionsForSync(fieldUser, sinceMillis);
        verify(mobileService, never()).listScopedInspectionsForSync(fieldUser, null);
    }

    @Test
    void build_invalidSyncToken_returnsFullDeltaWithWarning() {
        Inspection inspection = inspection(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        when(mobileService.listScopedInspectionsForSync(fieldUser, null)).thenReturn(List.of(inspection));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L)))
                .thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));

        var result = deltaService.build(fieldUser, "not-a-valid-token");

        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().get(0).getCode()).isEqualTo(com.infratrack.mobile.sync.dto.SyncWarningCode.FULL_SYNC_REQUIRED);
        assertThat(result.delta().getInspections()).hasSize(1);
        verify(mobileService).listScopedInspectionsForSync(fieldUser, null);
    }

    @Test
    void build_onlyIncludesScopedInspectionsFromMobileService() {
        Inspection visible = inspection(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        when(mobileService.listScopedInspectionsForSync(fieldUser, null)).thenReturn(List.of(visible));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L)))
                .thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));

        var result = deltaService.build(fieldUser, null);

        assertThat(result.delta().getInspections()).extracting("id").containsExactly(100L);
    }

    @Test
    void build_batchLoadsAnswersForMultipleInspections() {
        Inspection first = inspection(100L, 20L, 5_000L);
        Inspection second = inspection(200L, 20L, 6_000L);
        User fieldUser = user(20L);
        InspectionAnswer firstAnswer = answer(first, 1L, "value");
        InspectionAnswer secondAnswer = answer(second, 2L, "other");

        when(mobileService.listScopedInspectionsForSync(eq(fieldUser), isNull()))
                .thenReturn(List.of(first, second));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L, 200L)))
                .thenReturn(List.of(firstAnswer, secondAnswer));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));

        var result = deltaService.build(fieldUser, null);

        assertThat(result.delta().getInspections()).hasSize(2);
        assertThat(result.delta().getInspections().get(0).getAnswers()).hasSize(1);
        assertThat(result.delta().getInspections().get(0).getAnswers().get(0).getTextValue()).isEqualTo("value");
        assertThat(result.delta().getInspections().get(1).getAnswers()).hasSize(1);
        assertThat(result.delta().getInspections().get(1).getAnswers().get(0).getQuestionId()).isEqualTo(2L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> inspectionIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(inspectionAnswerRepository).findByInspectionIdInOrderByQuestionDisplayOrder(inspectionIdsCaptor.capture());
        assertThat(inspectionIdsCaptor.getValue()).containsExactly(100L, 200L);
        verify(inspectionAnswerRepository, never()).findByInspectionIdOrderByQuestionDisplayOrder(any());
    }

    @Test
    void build_templatedInspectionIncludesTemplateQuestionsAndChoices() {
        Inspection inspection = templatedInspection(100L, 20L);
        User fieldUser = user(20L);
        InspectionTemplateQuestion question = templateQuestion(10L, inspection.getInspectionTemplate());
        InspectionTemplateQuestionChoice choice = templateChoice(1L, question, "YES", "Yes");
        InspectionAnswer answer = choiceAnswer(inspection, question, "YES", 1L);

        SyncInspectionTemplateDeltaResponse template =
                SyncInspectionTemplateDeltaResponse.from(inspection.getInspectionTemplate());
        List<SyncInspectionQuestionDeltaResponse> questions = List.of(
                SyncInspectionQuestionDeltaResponse.from(
                        question,
                        List.of(SyncInspectionChoiceDeltaResponse.from(choice))));
        MobileInspectionChecklistLoader.ChecklistPayload payload =
                new MobileInspectionChecklistLoader.ChecklistPayload(
                        Map.of(TEMPLATE_ID, questions),
                        Map.of(10L, Map.of("YES", 1L)));

        when(mobileService.listScopedInspectionsForSync(fieldUser, null)).thenReturn(List.of(inspection));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L)))
                .thenReturn(List.of(answer));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(checklistLoader.loadChecklistPayloadByTemplateIds(Set.of(TEMPLATE_ID))).thenReturn(payload);

        var result = deltaService.build(fieldUser, null);
        var deltaInspection = result.delta().getInspections().get(0);

        assertThat(deltaInspection.getTemplate().getTemplateId()).isEqualTo(template.getTemplateId());
        assertThat(deltaInspection.getTemplate().getTemplateName()).isEqualTo(template.getTemplateName());
        assertThat(deltaInspection.getTemplate().getTemplateVersion()).isEqualTo(template.getTemplateVersion());
        assertThat(deltaInspection.getQuestions()).hasSize(1);
        assertThat(deltaInspection.getQuestions().get(0).getQuestionId()).isEqualTo(10L);
        assertThat(deltaInspection.getQuestions().get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(deltaInspection.getQuestions().get(0).getChoices()).hasSize(1);
        assertThat(deltaInspection.getQuestions().get(0).getChoices().get(0).getChoiceId()).isEqualTo(1L);
        assertThat(deltaInspection.getAnswers()).hasSize(1);
        assertThat(deltaInspection.getAnswers().get(0).getChoiceCodeValue()).isEqualTo("YES");
        assertThat(deltaInspection.getAnswers().get(0).getChoiceId()).isEqualTo(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<Long>> templateIdsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(checklistLoader).loadChecklistPayloadByTemplateIds(templateIdsCaptor.capture());
        assertThat(templateIdsCaptor.getValue()).containsExactly(TEMPLATE_ID);
        verify(mobileService, never()).getInspectionBundle(any(), any());
    }

    @Test
    void build_nonChoiceQuestionHasEmptyChoices() {
        Inspection inspection = templatedInspection(100L, 20L);
        User fieldUser = user(20L);
        InspectionTemplateQuestion question = templateQuestion(10L, inspection.getInspectionTemplate());
        question.setQuestionType(InspectionTemplateQuestionType.BOOLEAN);

        List<SyncInspectionQuestionDeltaResponse> questions = List.of(
                SyncInspectionQuestionDeltaResponse.from(question, List.of()));
        MobileInspectionChecklistLoader.ChecklistPayload payload =
                new MobileInspectionChecklistLoader.ChecklistPayload(
                        Map.of(TEMPLATE_ID, questions), Map.of());

        when(mobileService.listScopedInspectionsForSync(fieldUser, null)).thenReturn(List.of(inspection));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L)))
                .thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(checklistLoader.loadChecklistPayloadByTemplateIds(Set.of(TEMPLATE_ID))).thenReturn(payload);

        var result = deltaService.build(fieldUser, null);

        assertThat(result.delta().getInspections().get(0).getQuestions().get(0).getChoices()).isEmpty();
    }

    @Test
    void build_multipleInspectionsSharingTemplateBatchLoadsChecklistOnce() {
        Inspection first = templatedInspection(100L, 20L);
        Inspection second = templatedInspection(200L, 20L);
        User fieldUser = user(20L);

        when(mobileService.listScopedInspectionsForSync(fieldUser, null)).thenReturn(List.of(first, second));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L, 200L)))
                .thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));

        deltaService.build(fieldUser, null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<Long>> templateIdsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(checklistLoader).loadChecklistPayloadByTemplateIds(templateIdsCaptor.capture());
        assertThat(templateIdsCaptor.getValue()).containsExactly(TEMPLATE_ID);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("field@test.com");
        user.setRole(UserRole.FIELD_EMPLOYEE);
        return user;
    }

    private Inspection inspection(Long id, Long assignedToUserId, long updatedAt) {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L);
        asset.setId(50L);
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.SCHEDULED_INSPECTION,
                "Routine inspection",
                false,
                10L);
        trigger.setId(1L);
        Inspection inspection = new Inspection(
                asset,
                trigger,
                assignedToUserId,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7));
        inspection.setId(id);
        setUpdatedAt(inspection, updatedAt);
        return inspection;
    }

    private Inspection templatedInspection(Long id, Long assignedToUserId) {
        Inspection inspection = inspection(id, assignedToUserId, 5_000L);
        InspectionTemplate template = new InspectionTemplate(
                "Daily Checklist",
                "Daily asset inspection",
                categoryFromInspection(inspection),
                3,
                InspectionTemplateStatus.PUBLISHED);
        template.setId(TEMPLATE_ID);
        inspection.setInspectionTemplate(template);
        return inspection;
    }

    private AssetCategory categoryFromInspection(Inspection inspection) {
        return inspection.getAsset().getAssetCategory();
    }

    private InspectionTemplateQuestion templateQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Is equipment safe?",
                "SAFE",
                "Check equipment safety",
                InspectionTemplateQuestionType.CHOICE,
                true,
                1);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestionChoice templateChoice(
            Long id, InspectionTemplateQuestion question, String code, String label) {
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(question, code, label, 1);
        choice.setId(id);
        return choice;
    }

    private InspectionAnswer answer(Inspection inspection, long questionId, String textValue) {
        InspectionTemplateQuestion question = mock(InspectionTemplateQuestion.class);
        when(question.getId()).thenReturn(questionId);
        InspectionAnswer answer = mock(InspectionAnswer.class);
        when(answer.getInspection()).thenReturn(inspection);
        when(answer.getQuestion()).thenReturn(question);
        when(answer.getTextValue()).thenReturn(textValue);
        when(answer.getBooleanValue()).thenReturn(null);
        when(answer.getNumberValue()).thenReturn(null);
        when(answer.getChoiceCodeValue()).thenReturn(null);
        return answer;
    }

    private InspectionAnswer choiceAnswer(
            Inspection inspection, InspectionTemplateQuestion question, String code, Long choiceId) {
        InspectionAnswer answer = mock(InspectionAnswer.class);
        when(answer.getInspection()).thenReturn(inspection);
        when(answer.getQuestion()).thenReturn(question);
        when(answer.getChoiceCodeValue()).thenReturn(code);
        when(answer.getBooleanValue()).thenReturn(null);
        when(answer.getTextValue()).thenReturn(null);
        when(answer.getNumberValue()).thenReturn(null);
        return answer;
    }

    private static void setUpdatedAt(Inspection inspection, long updatedAt) {
        try {
            Field field = Inspection.class.getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(inspection, updatedAt);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

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
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    private static final Instant TOKEN_ISSUED_AT = Instant.parse("2026-07-05T08:00:00Z");

    @Mock
    private MobileService mobileService;

    @Mock
    private InspectionAnswerRepository inspectionAnswerRepository;

    @Mock
    private UserNameLookup userNameLookup;

    private InspectionSyncDeltaService deltaService;

    @BeforeEach
    void setUp() {
        deltaService = new InspectionSyncDeltaService(
                mobileService,
                inspectionAnswerRepository,
                userNameLookup);
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
        Inspection changed = inspection(100L, 20L, TOKEN_ISSUED_AT.toEpochMilli() + 1_000);
        User fieldUser = user(20L);
        long sinceMillis = TOKEN_ISSUED_AT.toEpochMilli();
        when(mobileService.listScopedInspectionsForSync(fieldUser, sinceMillis)).thenReturn(List.of(changed));
        when(inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(List.of(100L)))
                .thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));

        String syncToken = SyncToken.issue(TOKEN_ISSUED_AT).toOpaqueValue();
        var result = deltaService.build(fieldUser, syncToken);

        assertThat(result.warnings()).isEmpty();
        assertThat(result.delta().getInspections()).hasSize(1);
        assertThat(result.delta().getInspections().get(0).getId()).isEqualTo(100L);
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
        assertThat(result.warnings().get(0).getCode()).isEqualTo(SyncWarningCode.FULL_SYNC_REQUIRED);
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

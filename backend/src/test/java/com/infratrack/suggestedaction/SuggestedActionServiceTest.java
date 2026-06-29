package com.infratrack.suggestedaction;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.ruleevaluation.RuleEngineVersion;
import com.infratrack.ruleevaluation.RuleEvaluationReport;
import com.infratrack.ruleevaluation.RuleEvaluationResult;
import com.infratrack.ruleevaluation.RuleEvaluationStatus;
import com.infratrack.suggestedaction.dto.SuggestedActionResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestedActionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private SuggestedActionRepository suggestedActionRepository;

    @Mock
    private InspectionAuthorizationService authorizationService;

    @Mock
    private UserService userService;

    private SuggestedActionService suggestedActionService;

    @BeforeEach
    void setUp() {
        suggestedActionService = new SuggestedActionService(
                inspectionRepository,
                suggestedActionRepository,
                authorizationService,
                userService);
    }

    @Test
    void listSuggestedActions_shouldReturnActionsForVisibleInspection() {
        Inspection inspection = templatedInspection();
        User viewer = user(20L, UserRole.MANAGER);
        SuggestedAction action = pendingAction(inspection);

        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(viewer);
        when(suggestedActionRepository.findByInspectionWithOptionalFilters(100L, null, null))
                .thenReturn(List.of(action));

        List<SuggestedActionResponse> responses =
                suggestedActionService.listSuggestedActions(100L, 20L, null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("High temperature detected");
        assertThat(responses.get(0).getSeverity()).isEqualTo("HIGH");
        verify(authorizationService).requireCanViewInspection(viewer, inspection);
    }

    @Test
    void listSuggestedActions_shouldRejectCrossDepartmentAccess() {
        Inspection inspection = templatedInspection();
        User otherDepartmentUser = user(99L, UserRole.MANAGER);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(99L)).thenReturn(otherDepartmentUser);
        org.mockito.Mockito.doThrow(new ForbiddenOperationException(
                        "You may only view inspections for assets in your own department."))
                .when(authorizationService).requireCanViewInspection(otherDepartmentUser, inspection);

        assertThatThrownBy(() -> suggestedActionService.listSuggestedActions(100L, 99L, null, null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getSuggestedAction_shouldReturnDetail() {
        Inspection inspection = templatedInspection();
        User viewer = user(20L, UserRole.OPERATIONAL_COORDINATOR);
        SuggestedAction action = pendingAction(inspection);
        action.setId(7L);

        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(viewer);
        when(suggestedActionRepository.findByIdAndInspection_Id(7L, 100L)).thenReturn(Optional.of(action));

        SuggestedActionResponse response =
                suggestedActionService.getSuggestedAction(100L, 7L, 20L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getSourceRuleCodes()).isEqualTo("HIGH_TEMP");
        assertThat(response.getActionType()).isEqualTo(DecisionRuleActionType.SUGGEST_ISSUE);
    }

    @Test
    void getSuggestedAction_shouldRejectWhenNotFound() {
        Inspection inspection = templatedInspection();
        User viewer = user(20L, UserRole.OPERATIONAL_COORDINATOR);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(viewer);
        when(suggestedActionRepository.findByIdAndInspection_Id(7L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> suggestedActionService.getSuggestedAction(100L, 7L, 20L))
                .isInstanceOf(NotFoundException.class);
    }

    private Inspection templatedInspection() {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Pump");
        category.setId(2L);
        Asset asset = new Asset(
                "Pump Station",
                department,
                category,
                "Site A",
                AssetStatus.ACTIVE,
                null,
                1L);
        asset.setId(5L);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Checklist", null, category, 1, InspectionTemplateStatus.PUBLISHED);
        template.setId(50L);
        Inspection inspection = new Inspection(
                asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        inspection.setId(100L);
        inspection.setInspectionTemplate(template);
        return inspection;
    }

    private SuggestedAction pendingAction(Inspection inspection) {
        RuleEvaluationReport report = new RuleEvaluationReport(
                inspection,
                1_700_000_000_000L,
                RuleEngineVersion.CURRENT,
                5L,
                1,
                1);
        report.setId(1L);
        report.setEvaluationStatus(RuleEvaluationStatus.SUCCESS);
        RuleEvaluationResult result = new RuleEvaluationResult(
                1L,
                "HIGH_TEMP",
                "High temperature",
                com.infratrack.inspectiontemplate.DecisionRuleConditionType.NUMBER,
                com.infratrack.inspectiontemplate.DecisionRuleOperator.GREATER_THAN,
                "90",
                "95",
                true,
                DecisionRuleActionType.SUGGEST_ISSUE,
                "{\"severity\":\"HIGH\"}",
                10,
                1_700_000_000_000L,
                2L);
        return new SuggestedAction(
                inspection,
                report,
                result,
                DecisionRuleActionType.SUGGEST_ISSUE,
                "High temperature detected",
                "Temperature exceeds safe operating range.",
                "HIGH",
                "{\"severity\":\"HIGH\"}",
                1,
                "HIGH_TEMP",
                SuggestionConfidence.LOW);
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        Department department = new Department("Parks");
        department.setId(1L);
        user.setDepartment(department);
        return user;
    }
}

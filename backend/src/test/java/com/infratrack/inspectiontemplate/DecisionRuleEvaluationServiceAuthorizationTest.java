package com.infratrack.inspectiontemplate;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspectiontemplate.dto.DecisionRuleEvaluationResult;
import com.infratrack.organization.policy.visibility.InspectionVisibilityPolicyService;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionRuleEvaluationServiceAuthorizationTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionAnswerRepository answerRepository;

    @Mock
    private InspectionTemplateQuestionRuleRepository ruleRepository;

    @Mock
    private UserService userService;

    private DecisionRuleEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new DecisionRuleEvaluationService(
                inspectionRepository,
                answerRepository,
                ruleRepository,
                new InspectionAuthorizationService(userService, new InspectionVisibilityPolicyService("DEPARTMENT")),
                userService);
    }

    @Test
    void evaluateInspection_shouldAllowAdministrator() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        User administrator = user(1L, UserRole.ADMINISTRATOR, null);

        when(userService.getById(1L)).thenReturn(administrator);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L)).thenReturn(List.of());

        List<DecisionRuleEvaluationResult> results = evaluationService.evaluateInspection(100L, 1L);

        assertThat(results).isEmpty();
    }

    @Test
    void evaluateInspection_shouldAllowManagerForOwnDepartment() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        User manager = user(30L, UserRole.MANAGER, asset.getDepartment());

        when(userService.getById(30L)).thenReturn(manager);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L)).thenReturn(List.of());

        List<DecisionRuleEvaluationResult> results = evaluationService.evaluateInspection(100L, 30L);

        assertThat(results).isEmpty();
    }

    @Test
    void evaluateInspection_shouldRejectManagerForCrossDepartment() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        Department otherDepartment = new Department("Water");
        otherDepartment.setId(99L);
        User manager = user(30L, UserRole.MANAGER, otherDepartment);

        when(userService.getById(30L)).thenReturn(manager);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> evaluationService.evaluateInspection(100L, 30L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view inspections for assets in your own department.");

        verify(answerRepository, never()).findByInspectionIdOrderByQuestionDisplayOrder(anyLong());
        verify(ruleRepository, never()).findByQuestionIdInAndActiveTrueOrderByPriorityAscRuleCodeAsc(any());
    }

    @Test
    void evaluateInspection_shouldAllowAssignedFieldEmployeeInSameDepartment() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE, asset.getDepartment());

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L)).thenReturn(List.of());

        List<DecisionRuleEvaluationResult> results = evaluationService.evaluateInspection(100L, 20L);

        assertThat(results).isEmpty();
    }

    @Test
    void evaluateInspection_shouldRejectUnassignedFieldEmployeeForCrossDepartment() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        Department otherDepartment = new Department("Water");
        otherDepartment.setId(99L);
        User fieldEmployee = user(99L, UserRole.FIELD_EMPLOYEE, otherDepartment);

        when(userService.getById(99L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> evaluationService.evaluateInspection(100L, 99L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view inspections for assets in your own department.");

        verify(answerRepository, never()).findByInspectionIdOrderByQuestionDisplayOrder(anyLong());
    }

    @Test
    void evaluateInspection_shouldAllowContractorInSameDepartment() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 25L);
        User contractor = user(25L, UserRole.CONTRACTOR, asset.getDepartment());

        when(userService.getById(25L)).thenReturn(contractor);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L)).thenReturn(List.of());

        List<DecisionRuleEvaluationResult> results = evaluationService.evaluateInspection(100L, 25L);

        assertThat(results).isEmpty();
    }

    @Test
    void evaluateInspection_shouldReturnNotFoundForUnknownInspection() {
        when(inspectionRepository.findWithEvaluationContextById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluationService.evaluateInspection(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Inspection not found");

        verify(userService, never()).getById(anyLong());
        verify(answerRepository, never()).findByInspectionIdOrderByQuestionDisplayOrder(anyLong());
    }

    private Asset asset(Long id) {
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
                LocalDate.of(2026, 1, 1),
                1L);
        asset.setId(id);
        return asset;
    }

    private Inspection inspection(Long id, Asset asset, Long assignedToUserId) {
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
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
        return inspection;
    }

    private User user(Long id, UserRole role, Department department) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        user.setDepartment(department);
        return user;
    }
}

package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.preventivemaintenance.dto.CreatePreventiveMaintenancePlanRequest;
import com.infratrack.preventivemaintenance.dto.PlanBusinessTriggerRequest;
import com.infratrack.preventivemaintenance.dto.PreventiveMaintenancePlanResponse;
import com.infratrack.preventivemaintenance.dto.UpdatePreventiveMaintenancePlanRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreventiveMaintenancePlanServiceTest {

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    @Mock
    private PreventiveMaintenancePlanRepository planRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private InspectionTemplateRepository inspectionTemplateRepository;

    @InjectMocks
    private PreventiveMaintenancePlanService planService;

    @Test
    void create_shouldPersistPlanWithBusinessTrigger() {
        CreatePreventiveMaintenancePlanRequest request = createRequest();
        Asset asset = asset(5L, "Pump A");
        InspectionTemplate template = template(20L, "Pump Monthly Inspection");

        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionTemplateRepository.findById(20L)).thenReturn(Optional.of(template));
        when(planRepository.existsByPlanCode("PUMP_MONTHLY")).thenReturn(false);
        when(planRepository.save(any(PreventiveMaintenancePlan.class))).thenAnswer(invocation -> {
            PreventiveMaintenancePlan plan = invocation.getArgument(0);
            plan.setId(100L);
            plan.getBusinessTrigger().setId(200L);
            return plan;
        });

        PreventiveMaintenancePlanResponse response = planService.create(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getPlanCode()).isEqualTo("PUMP_MONTHLY");
        assertThat(response.getVersion()).isEqualTo(1);
        assertThat(response.getName()).isEqualTo("Monthly Pump Inspection");
        assertThat(response.getAssetId()).isEqualTo(5L);
        assertThat(response.getStatus()).isEqualTo(PreventiveMaintenancePlanStatus.ACTIVE);
        assertThat(response.getTargetAction()).isEqualTo(PlanTargetAction.CREATE_INSPECTION);
        assertThat(response.getInspectionTemplateId()).isEqualTo(20L);
        assertThat(response.getBusinessTrigger().getTriggerType()).isEqualTo(PlanTriggerType.TIME);
        assertThat(response.getBusinessTrigger().getConfigurationJson())
                .isEqualTo("{\"every\":1,\"unit\":\"MONTH\"}");
        assertThat(response.getBusinessTrigger().getTriggerSummary().getTitle()).isEqualTo("Every month");

        ArgumentCaptor<PreventiveMaintenancePlan> captor = ArgumentCaptor.forClass(PreventiveMaintenancePlan.class);
        verify(planRepository).save(captor.capture());
        assertThat(captor.getValue().getBusinessTrigger()).isNotNull();
        assertThat(captor.getValue().getBusinessTrigger().isActive()).isTrue();
    }

    @Test
    void create_shouldAllowNullInspectionTemplate() {
        CreatePreventiveMaintenancePlanRequest request = createRequest();
        request.setInspectionTemplateId(null);

        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset(5L, "Pump A")));
        when(planRepository.existsByPlanCode("PUMP_MONTHLY")).thenReturn(false);
        when(planRepository.save(any(PreventiveMaintenancePlan.class))).thenAnswer(invocation -> {
            PreventiveMaintenancePlan plan = invocation.getArgument(0);
            plan.setId(100L);
            return plan;
        });

        PreventiveMaintenancePlanResponse response = planService.create(request);

        assertThat(response.getInspectionTemplateId()).isNull();
        verify(inspectionTemplateRepository, never()).findById(any());
    }

    @Test
    void create_shouldRejectInvalidTriggerJson() {
        CreatePreventiveMaintenancePlanRequest request = createRequest();
        request.setInspectionTemplateId(null);
        request.getBusinessTrigger().setConfigurationJson("{\"every\":1,\"unit\":\"MINUTE\"}");

        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset(5L, "Pump A")));
        when(planRepository.existsByPlanCode("PUMP_MONTHLY")).thenReturn(false);

        assertThatThrownBy(() -> planService.create(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Unsupported trigger unit");

        verify(planRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectMissingAsset() {
        CreatePreventiveMaintenancePlanRequest request = createRequest();
        when(assetRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Asset not found");
    }

    @Test
    void update_shouldUpdatePlanAndTrigger() {
        UpdatePreventiveMaintenancePlanRequest request = updateRequest();
        PreventiveMaintenancePlan plan = plan(100L, PreventiveMaintenancePlanStatus.ACTIVE);

        when(planRepository.findDetailedById(100L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(PreventiveMaintenancePlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PreventiveMaintenancePlanResponse response = planService.update(100L, request);

        assertThat(response.getName()).isEqualTo("Updated Monthly Pump Inspection");
        assertThat(response.getBusinessTrigger().getTriggerType()).isEqualTo(PlanTriggerType.METER);
        assertThat(plan.getBusinessTrigger().getConfigurationJson())
                .isEqualTo("{\"meter\":\"OPERATING_HOURS\",\"every\":250}");
    }

    @Test
    void update_shouldRejectArchivedPlan() {
        UpdatePreventiveMaintenancePlanRequest request = updateRequest();
        when(planRepository.findDetailedById(100L))
                .thenReturn(Optional.of(plan(100L, PreventiveMaintenancePlanStatus.ARCHIVED)));

        assertThatThrownBy(() -> planService.update(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Archived preventive maintenance plans cannot be modified");
    }

    @Test
    void archive_shouldSetStatusToArchived() {
        PreventiveMaintenancePlan plan = plan(100L, PreventiveMaintenancePlanStatus.ACTIVE);
        when(planRepository.findDetailedById(100L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(PreventiveMaintenancePlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PreventiveMaintenancePlanResponse response = planService.archive(100L);

        assertThat(response.getStatus()).isEqualTo(PreventiveMaintenancePlanStatus.ARCHIVED);
    }

    @Test
    void archive_shouldRemainRetrievable() {
        PreventiveMaintenancePlan plan = plan(100L, PreventiveMaintenancePlanStatus.ARCHIVED);
        when(planRepository.findDetailedById(100L)).thenReturn(Optional.of(plan));

        PreventiveMaintenancePlanResponse response = planService.getById(100L);

        assertThat(response.getStatus()).isEqualTo(PreventiveMaintenancePlanStatus.ARCHIVED);
    }

    @Test
    void listPage_shouldFilterByAssetStatusAndTriggerType() {
        when(planRepository.findFiltered(
                eq(5L),
                eq(PreventiveMaintenancePlanStatus.ACTIVE),
                eq(PlanTriggerType.TIME),
                eq(DEFAULT_PAGEABLE)))
                .thenReturn(new PageImpl<>(List.of(plan(100L, PreventiveMaintenancePlanStatus.ACTIVE)), DEFAULT_PAGEABLE, 1));

        Page<PreventiveMaintenancePlanResponse> page = planService.listPage(
                5L,
                PreventiveMaintenancePlanStatus.ACTIVE,
                PlanTriggerType.TIME,
                DEFAULT_PAGEABLE);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void create_shouldRejectDuplicatePlanCode() {
        CreatePreventiveMaintenancePlanRequest request = createRequest();
        request.setInspectionTemplateId(null);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset(5L, "Pump A")));
        when(planRepository.existsByPlanCode("PUMP_MONTHLY")).thenReturn(true);

        assertThatThrownBy(() -> planService.create(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Plan code already exists");
    }

    @Test
    void create_shouldDefaultVersionToOne() {
        CreatePreventiveMaintenancePlanRequest request = createRequest();
        request.setInspectionTemplateId(null);
        request.setVersion(null);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset(5L, "Pump A")));
        when(planRepository.existsByPlanCode("PUMP_MONTHLY")).thenReturn(false);
        when(planRepository.save(any(PreventiveMaintenancePlan.class))).thenAnswer(invocation -> {
            PreventiveMaintenancePlan plan = invocation.getArgument(0);
            plan.setId(100L);
            return plan;
        });

        PreventiveMaintenancePlanResponse response = planService.create(request);

        assertThat(response.getVersion()).isEqualTo(1);
    }

    @Test
    void create_shouldRejectNonPositiveVersion() {
        CreatePreventiveMaintenancePlanRequest request = createRequest();
        request.setInspectionTemplateId(null);
        request.setVersion(0);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset(5L, "Pump A")));
        when(planRepository.existsByPlanCode("PUMP_MONTHLY")).thenReturn(false);

        assertThatThrownBy(() -> planService.create(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Plan version must be a positive integer");
    }

    @Test
    void getById_shouldRejectMissingPlan() {
        when(planRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Preventive maintenance plan not found");
    }

    private CreatePreventiveMaintenancePlanRequest createRequest() {
        CreatePreventiveMaintenancePlanRequest request = new CreatePreventiveMaintenancePlanRequest();
        request.setName("Monthly Pump Inspection");
        request.setPlanCode("PUMP_MONTHLY");
        request.setDescription("Monthly preventive inspection");
        request.setAssetId(5L);
        request.setStatus(PreventiveMaintenancePlanStatus.ACTIVE);
        request.setPriority(PreventiveMaintenancePlanPriority.MEDIUM);
        request.setTargetAction(PlanTargetAction.CREATE_INSPECTION);
        request.setInspectionTemplateId(20L);

        PlanBusinessTriggerRequest trigger = new PlanBusinessTriggerRequest();
        trigger.setTriggerType(PlanTriggerType.TIME);
        trigger.setConfigurationJson("{\"every\":1,\"unit\":\"MONTH\"}");
        trigger.setActive(true);
        request.setBusinessTrigger(trigger);
        return request;
    }

    private UpdatePreventiveMaintenancePlanRequest updateRequest() {
        UpdatePreventiveMaintenancePlanRequest request = new UpdatePreventiveMaintenancePlanRequest();
        request.setName("Updated Monthly Pump Inspection");
        request.setVersion(2);
        request.setDescription("Updated description");
        request.setStatus(PreventiveMaintenancePlanStatus.PAUSED);
        request.setPriority(PreventiveMaintenancePlanPriority.HIGH);
        request.setTargetAction(PlanTargetAction.CREATE_WORK_ORDER);
        request.setInspectionTemplateId(null);

        PlanBusinessTriggerRequest trigger = new PlanBusinessTriggerRequest();
        trigger.setTriggerType(PlanTriggerType.METER);
        trigger.setConfigurationJson("{\"meter\":\"OPERATING_HOURS\",\"every\":250}");
        trigger.setActive(false);
        request.setBusinessTrigger(trigger);
        return request;
    }

    private Asset asset(Long id, String name) {
        Asset asset = new Asset(
                name,
                mock(Department.class),
                mock(AssetCategory.class),
                "Location",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 1, 1),
                1L
        );
        asset.setId(id);
        return asset;
    }

    private InspectionTemplate template(Long id, String name) {
        InspectionTemplate template = new InspectionTemplate(
                name,
                null,
                mock(com.infratrack.assetcategory.AssetCategory.class),
                1,
                InspectionTemplateStatus.PUBLISHED
        );
        template.setId(id);
        return template;
    }

    private PreventiveMaintenancePlan plan(Long id, PreventiveMaintenancePlanStatus status) {
        PreventiveMaintenancePlan plan = new PreventiveMaintenancePlan(
                asset(5L, "Pump A"),
                "PUMP_MONTHLY",
                "Monthly Pump Inspection",
                "Monthly preventive inspection",
                1,
                status,
                PreventiveMaintenancePlanPriority.MEDIUM,
                PlanTargetAction.CREATE_INSPECTION,
                null
        );
        plan.setId(id);
        PlanBusinessTrigger trigger = new PlanBusinessTrigger(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}",
                true
        );
        trigger.setId(200L);
        plan.setBusinessTrigger(trigger);
        return plan;
    }
}

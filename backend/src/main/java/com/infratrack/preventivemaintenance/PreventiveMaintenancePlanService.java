package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateRepository;
import com.infratrack.preventivemaintenance.dto.CreatePreventiveMaintenancePlanRequest;
import com.infratrack.preventivemaintenance.dto.PlanBusinessTriggerRequest;
import com.infratrack.preventivemaintenance.dto.PreventiveMaintenancePlanResponse;
import com.infratrack.preventivemaintenance.dto.UpdatePreventiveMaintenancePlanRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages preventive maintenance plan definitions (V2 Phase B).
 * Configuration and trigger definition validation only — no scheduling or execution.
 */
@Service
public class PreventiveMaintenancePlanService {

    static final int INITIAL_VERSION = 1;

    private final PreventiveMaintenancePlanRepository planRepository;
    private final AssetRepository assetRepository;
    private final InspectionTemplateRepository inspectionTemplateRepository;

    public PreventiveMaintenancePlanService(
            PreventiveMaintenancePlanRepository planRepository,
            AssetRepository assetRepository,
            InspectionTemplateRepository inspectionTemplateRepository) {
        this.planRepository = planRepository;
        this.assetRepository = assetRepository;
        this.inspectionTemplateRepository = inspectionTemplateRepository;
    }

    @Transactional(readOnly = true)
    public Page<PreventiveMaintenancePlanResponse> listPage(
            Long assetId,
            PreventiveMaintenancePlanStatus status,
            PlanTriggerType triggerType,
            Pageable pageable) {
        return planRepository.findFiltered(assetId, status, triggerType, pageable)
                .map(PreventiveMaintenancePlanResponse::from);
    }

    @Transactional(readOnly = true)
    public PreventiveMaintenancePlanResponse getById(Long id) {
        return PreventiveMaintenancePlanResponse.from(findPlanOrThrow(id));
    }

    @Transactional
    public PreventiveMaintenancePlanResponse create(CreatePreventiveMaintenancePlanRequest request) {
        Asset asset = findAssetOrThrow(request.getAssetId());
        InspectionTemplate inspectionTemplate = resolveOptionalTemplate(request.getInspectionTemplateId());
        String planCode = PlanCodeValidator.validateAndNormalize(request.getPlanCode());
        requireUniquePlanCode(planCode);
        int version = resolveVersion(request.getVersion());

        PreventiveMaintenancePlan plan = new PreventiveMaintenancePlan(
                asset,
                planCode,
                normalizeName(request.getName()),
                normalizeOptionalDescription(request.getDescription()),
                version,
                request.getStatus(),
                request.getPriority(),
                request.getTargetAction(),
                inspectionTemplate
        );
        plan.setBusinessTrigger(buildTrigger(request.getBusinessTrigger()));

        PreventiveMaintenancePlan saved = planRepository.save(plan);
        return PreventiveMaintenancePlanResponse.from(saved);
    }

    @Transactional
    public PreventiveMaintenancePlanResponse update(Long id, UpdatePreventiveMaintenancePlanRequest request) {
        PreventiveMaintenancePlan plan = findPlanOrThrow(id);
        requireNotArchived(plan);

        plan.setName(normalizeName(request.getName()));
        plan.setDescription(normalizeOptionalDescription(request.getDescription()));
        plan.setVersion(requirePositiveVersion(request.getVersion()));
        plan.setStatus(request.getStatus());
        plan.setPriority(request.getPriority());
        plan.setTargetAction(request.getTargetAction());
        plan.setInspectionTemplate(resolveOptionalTemplate(request.getInspectionTemplateId()));
        applyTriggerUpdate(plan, request.getBusinessTrigger());
        plan.touchUpdatedAt();

        return PreventiveMaintenancePlanResponse.from(planRepository.save(plan));
    }

    @Transactional
    public PreventiveMaintenancePlanResponse archive(Long id) {
        PreventiveMaintenancePlan plan = findPlanOrThrow(id);
        plan.setStatus(PreventiveMaintenancePlanStatus.ARCHIVED);
        plan.touchUpdatedAt();
        return PreventiveMaintenancePlanResponse.from(planRepository.save(plan));
    }

    private PreventiveMaintenancePlan findPlanOrThrow(Long id) {
        return planRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Preventive maintenance plan not found"));
    }

    private Asset findAssetOrThrow(Long assetId) {
        if (assetId == null) {
            throw new BusinessValidationException("Asset is required");
        }
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
    }

    private InspectionTemplate resolveOptionalTemplate(Long inspectionTemplateId) {
        if (inspectionTemplateId == null) {
            return null;
        }
        return inspectionTemplateRepository.findById(inspectionTemplateId)
                .orElseThrow(() -> new NotFoundException("Inspection template not found"));
    }

    private void requireUniquePlanCode(String planCode) {
        if (planRepository.existsByPlanCode(planCode)) {
            throw new BusinessValidationException("Plan code already exists");
        }
    }

    private int resolveVersion(Integer version) {
        if (version == null) {
            return INITIAL_VERSION;
        }
        return requirePositiveVersion(version);
    }

    private int requirePositiveVersion(Integer version) {
        if (version == null || version <= 0) {
            throw new BusinessValidationException("Plan version must be a positive integer");
        }
        return version;
    }

    private PlanBusinessTrigger buildTrigger(PlanBusinessTriggerRequest request) {
        return new PlanBusinessTrigger(
                request.getTriggerType(),
                TriggerDefinitionValidator.validateAndNormalize(
                        request.getTriggerType(),
                        request.getConfigurationJson()),
                Boolean.TRUE.equals(request.getActive())
        );
    }

    private void applyTriggerUpdate(PreventiveMaintenancePlan plan, PlanBusinessTriggerRequest request) {
        PlanBusinessTrigger trigger = plan.getBusinessTrigger();
        if (trigger == null) {
            throw new BusinessValidationException("Preventive maintenance plan must have a business trigger");
        }
        trigger.setTriggerType(request.getTriggerType());
        trigger.setConfigurationJson(TriggerDefinitionValidator.validateAndNormalize(
                request.getTriggerType(),
                request.getConfigurationJson()));
        trigger.setActive(Boolean.TRUE.equals(request.getActive()));
        trigger.touchUpdatedAt();
    }

    private void requireNotArchived(PreventiveMaintenancePlan plan) {
        if (plan.getStatus() == PreventiveMaintenancePlanStatus.ARCHIVED) {
            throw new BusinessValidationException("Archived preventive maintenance plans cannot be modified");
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessValidationException("Plan name is required");
        }
        return name.trim();
    }

    private String normalizeOptionalDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}

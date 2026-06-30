package com.infratrack.inspectiontemplate;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateResponse;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages reusable inspection templates attached to asset categories (V2 Domain Engine A2.1).
 */
@Service
public class InspectionTemplateService {

    static final int INITIAL_VERSION = 1;

    static final String PUBLISH_REQUIRES_ACTIVE_QUESTIONS_MESSAGE =
            "Templates must contain at least one question before publication.";

    private final InspectionTemplateRepository inspectionTemplateRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final InspectionTemplateQuestionRepository inspectionTemplateQuestionRepository;

    public InspectionTemplateService(
            InspectionTemplateRepository inspectionTemplateRepository,
            AssetCategoryRepository assetCategoryRepository,
            InspectionTemplateQuestionRepository inspectionTemplateQuestionRepository) {
        this.inspectionTemplateRepository = inspectionTemplateRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.inspectionTemplateQuestionRepository = inspectionTemplateQuestionRepository;
    }

    @Transactional(readOnly = true)
    public Page<InspectionTemplateResponse> listPage(
            Long assetCategoryId,
            InspectionTemplateStatus status,
            Pageable pageable) {
        return inspectionTemplateRepository.findFiltered(assetCategoryId, status, pageable)
                .map(InspectionTemplateResponse::from);
    }

    @Transactional(readOnly = true)
    public InspectionTemplateResponse getById(Long id) {
        return InspectionTemplateResponse.from(findTemplateOrThrow(id));
    }

    @Transactional
    public InspectionTemplateResponse create(CreateInspectionTemplateRequest request) {
        String name = normalizeName(request.getName());
        String description = normalizeOptionalDescription(request.getDescription());
        AssetCategory assetCategory = findAssetCategoryOrThrow(request.getAssetCategoryId());

        InspectionTemplate template = inspectionTemplateRepository.save(new InspectionTemplate(
                name,
                description,
                assetCategory,
                INITIAL_VERSION,
                InspectionTemplateStatus.DRAFT
        ));
        return InspectionTemplateResponse.from(template);
    }

    @Transactional
    public InspectionTemplateResponse update(Long id, UpdateInspectionTemplateRequest request) {
        InspectionTemplate template = findTemplateOrThrow(id);
        requireDraftTemplate(template);

        template.setName(normalizeName(request.getName()));
        template.setDescription(normalizeOptionalDescription(request.getDescription()));
        template.touchUpdatedAt();
        return InspectionTemplateResponse.from(inspectionTemplateRepository.save(template));
    }

    @Transactional
    public InspectionTemplateResponse publish(Long id) {
        InspectionTemplate template = findTemplateOrThrow(id);
        requireDraftTemplate(template);
        requireAtLeastOneActiveQuestion(template.getId());

        template.setStatus(InspectionTemplateStatus.PUBLISHED);
        template.touchUpdatedAt();
        return InspectionTemplateResponse.from(inspectionTemplateRepository.save(template));
    }

    @Transactional
    public InspectionTemplateResponse archive(Long id) {
        InspectionTemplate template = findTemplateOrThrow(id);
        requirePublishedTemplate(template);

        template.setStatus(InspectionTemplateStatus.ARCHIVED);
        template.touchUpdatedAt();
        return InspectionTemplateResponse.from(inspectionTemplateRepository.save(template));
    }

    private InspectionTemplate findTemplateOrThrow(Long id) {
        return inspectionTemplateRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Inspection template not found"));
    }

    private AssetCategory findAssetCategoryOrThrow(Long assetCategoryId) {
        if (assetCategoryId == null) {
            throw new BusinessValidationException("Asset category is required");
        }
        return assetCategoryRepository.findById(assetCategoryId)
                .orElseThrow(() -> new NotFoundException("Asset category not found"));
    }

    private void requireDraftTemplate(InspectionTemplate template) {
        if (template.getStatus() == InspectionTemplateStatus.DRAFT) {
            return;
        }
        if (template.getStatus() == InspectionTemplateStatus.PUBLISHED) {
            throw new BusinessValidationException("Published inspection templates cannot be modified");
        }
        throw new BusinessValidationException("Archived inspection templates cannot be modified");
    }

    private void requirePublishedTemplate(InspectionTemplate template) {
        if (template.getStatus() == InspectionTemplateStatus.PUBLISHED) {
            return;
        }
        if (template.getStatus() == InspectionTemplateStatus.DRAFT) {
            throw new BusinessValidationException("Only published inspection templates can be archived");
        }
        throw new BusinessValidationException("Inspection template is already archived");
    }

    private void requireAtLeastOneActiveQuestion(Long templateId) {
        long activeQuestionCount = inspectionTemplateQuestionRepository
                .countByInspectionTemplateIdAndActiveTrue(templateId);
        if (activeQuestionCount < 1) {
            throw new BusinessValidationException(PUBLISH_REQUIRES_ACTIVE_QUESTIONS_MESSAGE);
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessValidationException("Template name is required");
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

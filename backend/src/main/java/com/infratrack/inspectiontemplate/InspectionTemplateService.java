package com.infratrack.inspectiontemplate;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
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

    private final InspectionTemplateRepository inspectionTemplateRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final InspectionTemplateQuestionRepository questionRepository;

    public InspectionTemplateService(
            InspectionTemplateRepository inspectionTemplateRepository,
            AssetCategoryRepository assetCategoryRepository,
            InspectionTemplateQuestionRepository questionRepository) {
        this.inspectionTemplateRepository = inspectionTemplateRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.questionRepository = questionRepository;
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
        requireDraft(template);

        template.setName(normalizeName(request.getName()));
        template.setDescription(normalizeOptionalDescription(request.getDescription()));
        template.touchUpdatedAt();
        return InspectionTemplateResponse.from(inspectionTemplateRepository.save(template));
    }

    @Transactional
    public InspectionTemplateResponse publish(Long id) {
        InspectionTemplate template = findTemplateOrThrow(id);
        requireDraft(template);

        int activeQuestions = questionRepository.countByInspectionTemplateIdAndActiveTrue(id);
        if (activeQuestions == 0) {
            throw new BusinessValidationException(
                    "Inspection template must have at least one active question before publishing");
        }

        template.setStatus(InspectionTemplateStatus.PUBLISHED);
        template.touchUpdatedAt();
        return InspectionTemplateResponse.from(inspectionTemplateRepository.save(template));
    }

    @Transactional
    public InspectionTemplateResponse archive(Long id) {
        InspectionTemplate template = findTemplateOrThrow(id);
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

    private void requireDraft(InspectionTemplate template) {
        if (template.getStatus() != InspectionTemplateStatus.DRAFT) {
            throw new ConflictException("Only draft inspection templates can be modified");
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

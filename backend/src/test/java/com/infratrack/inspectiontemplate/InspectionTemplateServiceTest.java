package com.infratrack.inspectiontemplate;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateResponse;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateRequest;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionTemplateServiceTest {

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    @Mock
    private InspectionTemplateRepository inspectionTemplateRepository;

    @Mock
    private AssetCategoryRepository assetCategoryRepository;

    @InjectMocks
    private InspectionTemplateService inspectionTemplateService;

    @Test
    void create_shouldCreateDraftTemplateWithVersionOne() {
        CreateInspectionTemplateRequest request = createRequest();
        AssetCategory category = assetCategory(10L, "Pump");

        when(assetCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(inspectionTemplateRepository.save(any(InspectionTemplate.class))).thenAnswer(invocation -> {
            InspectionTemplate template = invocation.getArgument(0);
            template.setId(100L);
            return template;
        });

        InspectionTemplateResponse response = inspectionTemplateService.create(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Pump Inspection Template");
        assertThat(response.getDescription()).isEqualTo("Standard pump inspection");
        assertThat(response.getAssetCategoryId()).isEqualTo(10L);
        assertThat(response.getAssetCategoryName()).isEqualTo("Pump");
        assertThat(response.getVersion()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(InspectionTemplateStatus.DRAFT);

        ArgumentCaptor<InspectionTemplate> captor = ArgumentCaptor.forClass(InspectionTemplate.class);
        verify(inspectionTemplateRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InspectionTemplateStatus.DRAFT);
        assertThat(captor.getValue().getVersion()).isEqualTo(1);
    }

    @Test
    void create_shouldRejectMissingAssetCategory() {
        CreateInspectionTemplateRequest request = createRequest();
        when(assetCategoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionTemplateService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Asset category not found");

        verify(inspectionTemplateRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectBlankName() {
        CreateInspectionTemplateRequest request = createRequest();
        request.setName("   ");

        assertThatThrownBy(() -> inspectionTemplateService.create(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Template name is required");
    }

    @Test
    void update_shouldUpdateMetadata() {
        UpdateInspectionTemplateRequest request = new UpdateInspectionTemplateRequest();
        request.setName("Updated Pump Template");
        request.setDescription("Updated description");

        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(inspectionTemplateRepository.save(any(InspectionTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionTemplateResponse response = inspectionTemplateService.update(100L, request);

        assertThat(response.getName()).isEqualTo("Updated Pump Template");
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void update_shouldRejectArchivedTemplate() {
        UpdateInspectionTemplateRequest request = new UpdateInspectionTemplateRequest();
        request.setName("Updated Pump Template");

        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.ARCHIVED)));

        assertThatThrownBy(() -> inspectionTemplateService.update(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Archived inspection templates cannot be modified");
    }

    @Test
    void archive_shouldSetStatusToArchived() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.PUBLISHED);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(inspectionTemplateRepository.save(any(InspectionTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionTemplateResponse response = inspectionTemplateService.archive(100L);

        assertThat(response.getStatus()).isEqualTo(InspectionTemplateStatus.ARCHIVED);
        verify(inspectionTemplateRepository).save(template);
    }

    @Test
    void archive_shouldRemainRetrievable() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        template.setStatus(InspectionTemplateStatus.ARCHIVED);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));

        InspectionTemplateResponse response = inspectionTemplateService.getById(100L);

        assertThat(response.getStatus()).isEqualTo(InspectionTemplateStatus.ARCHIVED);
    }

    @Test
    void listPage_shouldReturnPagedTemplates() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findFiltered(isNull(), isNull(), eq(DEFAULT_PAGEABLE)))
                .thenReturn(new PageImpl<>(List.of(template), DEFAULT_PAGEABLE, 1));

        Page<InspectionTemplateResponse> page = inspectionTemplateService.listPage(null, null, DEFAULT_PAGEABLE);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(100L);
    }

    @Test
    void listPage_shouldFilterByAssetCategoryAndStatus() {
        when(inspectionTemplateRepository.findFiltered(eq(10L), eq(InspectionTemplateStatus.DRAFT), eq(DEFAULT_PAGEABLE)))
                .thenReturn(new PageImpl<>(List.of(), DEFAULT_PAGEABLE, 0));

        inspectionTemplateService.listPage(10L, InspectionTemplateStatus.DRAFT, DEFAULT_PAGEABLE);

        verify(inspectionTemplateRepository).findFiltered(10L, InspectionTemplateStatus.DRAFT, DEFAULT_PAGEABLE);
    }

    @Test
    void getById_shouldRejectMissingTemplate() {
        when(inspectionTemplateRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionTemplateService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Inspection template not found");
    }

    private CreateInspectionTemplateRequest createRequest() {
        CreateInspectionTemplateRequest request = new CreateInspectionTemplateRequest();
        request.setName("Pump Inspection Template");
        request.setDescription("Standard pump inspection");
        request.setAssetCategoryId(10L);
        return request;
    }

    private AssetCategory assetCategory(Long id, String name) {
        AssetCategory category = new AssetCategory(name);
        category.setId(id);
        return category;
    }

    private InspectionTemplate template(Long id, InspectionTemplateStatus status) {
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection Template",
                "Standard pump inspection",
                assetCategory(10L, "Pump"),
                1,
                status
        );
        template.setId(id);
        return template;
    }
}

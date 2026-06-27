package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.workorder.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalDocumentOwnerResolverTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private OperationalDecisionRepository operationalDecisionRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private MaintenanceActivityRepository maintenanceActivityRepository;

    @Mock
    private CompletionReviewRepository completionReviewRepository;

    private OperationalDocumentOwnerResolver ownerResolver;

    @BeforeEach
    void setUp() {
        ownerResolver = new OperationalDocumentOwnerResolver(
                assetRepository,
                inspectionRepository,
                issueRepository,
                operationalDecisionRepository,
                workOrderRepository,
                maintenanceActivityRepository,
                completionReviewRepository);
    }

    @Test
    void resolve_shouldDefaultToAssetOwnerWhenOwnerTypeIsNull() {
        Asset asset = asset(5L);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));

        OperationalDocumentOwnerContext context = ownerResolver.resolve(5L, null, null);

        assertThat(context.asset()).isEqualTo(asset);
        assertThat(context.ownerType()).isEqualTo(OperationalDocumentOwnerType.ASSET);
        assertThat(context.ownerId()).isEqualTo(5L);
    }

    @Test
    void resolve_shouldRejectOwnerFromDifferentAsset() {
        Asset asset = asset(5L);
        Asset otherAsset = asset(99L);
        otherAsset.setId(99L);
        Inspection inspection = inspection(100L, otherAsset);

        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> ownerResolver.resolve(
                5L, OperationalDocumentOwnerType.INSPECTION, 100L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Operational owner belongs to another asset");
    }

    @Test
    void resolve_shouldRejectMissingAsset() {
        when(assetRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerResolver.resolve(5L, null, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Asset not found");
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

    private Inspection inspection(Long id, Asset asset) {
        return new Inspection(
                asset,
                null,
                20L,
                10L,
                InspectionPriority.NORMAL,
                null);
    }
}

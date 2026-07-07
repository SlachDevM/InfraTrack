package com.infratrack.mobile.sync;

import com.infratrack.assetcategory.AssetCategoryService;
import com.infratrack.assetcategory.dto.AssetCategoryResponse;
import com.infratrack.department.DepartmentService;
import com.infratrack.department.dto.DepartmentResponse;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.mobile.sync.dto.SyncReferenceDataDeltaResponse;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceDataSyncDeltaServiceTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-07-05T08:30:00Z");

    @Mock
    private AssetCategoryService assetCategoryService;

    @Mock
    private DepartmentService departmentService;

    private ReferenceDataSyncDeltaService deltaService;

    @BeforeEach
    void setUp() {
        deltaService = new ReferenceDataSyncDeltaService(assetCategoryService, departmentService);
    }

    @Test
    void buildSnapshot_includesReferenceRecordsAndEnumDictionaries() {
        when(assetCategoryService.listAll()).thenReturn(List.of(
                new AssetCategoryResponse(1L, "Playground", 1L, 2L),
                new AssetCategoryResponse(2L, "Road", 1L, 2L)));
        when(departmentService.listAll()).thenReturn(List.of(
                new DepartmentResponse(3L, "Parks", 1L, 2L)));

        SyncReferenceDataDeltaResponse result = deltaService.buildSnapshot(GENERATED_AT);

        assertThat(result.getGeneratedAt()).isEqualTo(GENERATED_AT.toEpochMilli());
        assertThat(result.getSchemaVersion()).isEqualTo(SyncReferenceDataDeltaResponse.CURRENT_SCHEMA_VERSION);
        assertThat(result.getAssetCategories()).hasSize(2);
        assertThat(result.getAssetCategories().get(0).getId()).isEqualTo(1L);
        assertThat(result.getAssetCategories().get(0).getLabel()).isEqualTo("Playground");
        assertThat(result.getAssetCategories().get(0).getCode()).isNull();
        assertThat(result.getAssetCategories().get(0).getActive()).isNull();
        assertThat(result.getDepartments()).hasSize(1);
        assertThat(result.getDepartments().get(0).getLabel()).isEqualTo("Parks");
        assertThat(result.getWorkOrderTypes()).hasSize(WorkType.values().length);
        assertThat(result.getInspectionStatuses()).hasSize(InspectionStatus.values().length);
        assertThat(result.getInspectionPriorities()).hasSize(InspectionPriority.values().length);
        assertThat(result.getWorkOrderStatuses()).hasSize(WorkOrderStatus.values().length);
        assertThat(result.getWorkOrderPriorities()).hasSize(WorkOrderPriority.values().length);
        assertThat(result.getAssetStatuses()).hasSize(4);
        assertThat(result.getIssueSeverities()).hasSize(IssueSeverity.values().length);
        assertThat(result.getInspectionStatuses())
                .anyMatch(item -> "ASSIGNED".equals(item.getCode()) && "Assigned".equals(item.getLabel()));
        assertThat(result.getWorkOrderTypes())
                .anyMatch(item -> "INTERNAL_MAINTENANCE".equals(item.getCode())
                        && "Internal Maintenance".equals(item.getLabel()));
        verify(assetCategoryService).listAll();
        verify(departmentService).listAll();
    }
}

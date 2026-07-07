package com.infratrack.mobile.sync;

import com.infratrack.assetcategory.AssetCategoryService;
import com.infratrack.assetcategory.dto.AssetCategoryResponse;
import com.infratrack.department.DepartmentService;
import com.infratrack.department.dto.DepartmentResponse;
import com.infratrack.mobile.sync.dto.SyncReferenceDataDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncReferenceItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Builds the reference data section of the mobile sync delta (M6.5-BE1).
 * Returns a full deployment snapshot on every sync handshake.
 */
@Service
class ReferenceDataSyncDeltaService {

    private final AssetCategoryService assetCategoryService;
    private final DepartmentService departmentService;

    ReferenceDataSyncDeltaService(
            AssetCategoryService assetCategoryService,
            DepartmentService departmentService) {
        this.assetCategoryService = assetCategoryService;
        this.departmentService = departmentService;
    }

    @Transactional(readOnly = true)
    SyncReferenceDataDeltaResponse buildSnapshot(Instant generatedAt) {
        SyncReferenceDataDeltaResponse response = new SyncReferenceDataDeltaResponse();
        response.setGeneratedAt(generatedAt.toEpochMilli());
        response.setSchemaVersion(SyncReferenceDataDeltaResponse.CURRENT_SCHEMA_VERSION);
        response.setAssetCategories(mapAssetCategories(assetCategoryService.listAll()));
        response.setDepartments(mapDepartments(departmentService.listAll()));
        response.setWorkOrderTypes(SyncReferenceDataLabels.workOrderTypes());
        response.setInspectionStatuses(SyncReferenceDataLabels.inspectionStatuses());
        response.setInspectionPriorities(SyncReferenceDataLabels.inspectionPriorities());
        response.setWorkOrderStatuses(SyncReferenceDataLabels.workOrderStatuses());
        response.setWorkOrderPriorities(SyncReferenceDataLabels.workOrderPriorities());
        response.setAssetStatuses(SyncReferenceDataLabels.assetStatuses());
        response.setIssueSeverities(SyncReferenceDataLabels.issueSeverities());
        return response;
    }

    private static List<SyncReferenceItemResponse> mapAssetCategories(List<AssetCategoryResponse> categories) {
        return categories.stream()
                .map(category -> new SyncReferenceItemResponse(category.getId(), category.getName()))
                .toList();
    }

    private static List<SyncReferenceItemResponse> mapDepartments(List<DepartmentResponse> departments) {
        return departments.stream()
                .map(department -> new SyncReferenceItemResponse(department.getId(), department.getName()))
                .toList();
    }
}

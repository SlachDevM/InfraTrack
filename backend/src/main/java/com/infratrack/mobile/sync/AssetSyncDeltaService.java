package com.infratrack.mobile.sync;

import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.sync.dto.SyncAssetDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncWorkOrderDeltaResponse;
import com.infratrack.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the asset context section of the mobile sync delta (M6.3-BE1).
 */
@Service
class AssetSyncDeltaService {

    private final MobileService mobileService;

    AssetSyncDeltaService(MobileService mobileService) {
        this.mobileService = mobileService;
    }

    @Transactional(readOnly = true)
    List<SyncAssetDeltaResponse> buildDeltaRecords(
            User user,
            List<SyncInspectionDeltaResponse> inspections,
            List<SyncWorkOrderDeltaResponse> workOrders) {
        Set<Long> assetIds = collectRelevantAssetIds(inspections, workOrders);
        if (assetIds.isEmpty()) {
            return List.of();
        }
        return mobileService.buildAssetContextsForSync(user, assetIds).stream()
                .map(SyncAssetDeltaResponse::from)
                .toList();
    }

    static Set<Long> collectRelevantAssetIds(
            List<SyncInspectionDeltaResponse> inspections,
            List<SyncWorkOrderDeltaResponse> workOrders) {
        Set<Long> assetIds = new LinkedHashSet<>();
        if (inspections != null) {
            for (SyncInspectionDeltaResponse inspection : inspections) {
                if (inspection.getAsset() != null && inspection.getAsset().getAssetId() != null) {
                    assetIds.add(inspection.getAsset().getAssetId());
                }
            }
        }
        if (workOrders != null) {
            for (SyncWorkOrderDeltaResponse workOrder : workOrders) {
                if (workOrder.getAssetId() != null) {
                    assetIds.add(workOrder.getAssetId());
                }
            }
        }
        return assetIds;
    }
}

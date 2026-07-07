package com.infratrack.mobile.sync;

import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.dto.AssetContextResponse;
import com.infratrack.mobile.dto.AssetContextSummaryResponse;
import com.infratrack.mobile.dto.MobileAssetDocumentSummaryResponse;
import com.infratrack.mobile.sync.dto.SyncAssetDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionAssetDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncWorkOrderDeltaResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetSyncDeltaServiceTest {

    @Mock
    private MobileService mobileService;

    private AssetSyncDeltaService deltaService;

    @BeforeEach
    void setUp() {
        deltaService = new AssetSyncDeltaService(mobileService);
    }

    @Test
    void collectRelevantAssetIds_includesInspectionAndWorkOrderAssets() {
        SyncInspectionDeltaResponse inspection = new SyncInspectionDeltaResponse();
        SyncInspectionAssetDeltaResponse asset = new SyncInspectionAssetDeltaResponse();
        asset.setAssetId(10L);
        inspection.setAsset(asset);

        SyncWorkOrderDeltaResponse workOrder = new SyncWorkOrderDeltaResponse();
        workOrder.setAssetId(20L);

        Set<Long> assetIds = AssetSyncDeltaService.collectRelevantAssetIds(
                List.of(inspection), List.of(workOrder));

        assertThat(assetIds).containsExactly(10L, 20L);
    }

    @Test
    void collectRelevantAssetIds_deduplicatesSharedAsset() {
        SyncInspectionDeltaResponse inspection = new SyncInspectionDeltaResponse();
        SyncInspectionAssetDeltaResponse asset = new SyncInspectionAssetDeltaResponse();
        asset.setAssetId(10L);
        inspection.setAsset(asset);

        SyncWorkOrderDeltaResponse workOrder = new SyncWorkOrderDeltaResponse();
        workOrder.setAssetId(10L);

        Set<Long> assetIds = AssetSyncDeltaService.collectRelevantAssetIds(
                List.of(inspection), List.of(workOrder));

        assertThat(assetIds).containsExactly(10L);
    }

    @Test
    void collectRelevantAssetIds_emptyOperationalDeltasReturnsEmptySet() {
        assertThat(AssetSyncDeltaService.collectRelevantAssetIds(List.of(), List.of())).isEmpty();
    }

    @Test
    void buildDeltaRecords_delegatesToMobileServiceForRelevantAssetIds() {
        User fieldUser = user(20L);
        SyncInspectionDeltaResponse inspection = new SyncInspectionDeltaResponse();
        SyncInspectionAssetDeltaResponse asset = new SyncInspectionAssetDeltaResponse();
        asset.setAssetId(50L);
        inspection.setAsset(asset);

        AssetContextResponse context = assetContext(50L, "PL-001", "Playground");
        when(mobileService.buildAssetContextsForSync(eq(fieldUser), eq(Set.of(50L))))
                .thenReturn(List.of(context));

        List<SyncAssetDeltaResponse> result = deltaService.buildDeltaRecords(
                fieldUser, List.of(inspection), List.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAsset().getId()).isEqualTo(50L);
        assertThat(result.get(0).getAsset().getCode()).isNotBlank();
        verify(mobileService).buildAssetContextsForSync(fieldUser, Set.of(50L));
    }

    @Test
    void buildDeltaRecords_emptyWhenNoLinkedAssets() {
        User fieldUser = user(20L);

        List<SyncAssetDeltaResponse> result = deltaService.buildDeltaRecords(
                fieldUser, List.of(), List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void buildDeltaRecords_mapsDocumentMetadataWithoutStoragePaths() {
        User fieldUser = user(20L);
        SyncWorkOrderDeltaResponse workOrder = new SyncWorkOrderDeltaResponse();
        workOrder.setAssetId(50L);

        MobileAssetDocumentSummaryResponse document = new MobileAssetDocumentSummaryResponse();
        // MobileAssetDocumentSummaryResponse uses factory - use reflection or from OperationalDocument in integration
        // Test via AssetContextResponse built with document from factory in helper
        AssetContextResponse context = assetContextWithDocument(50L);
        when(mobileService.buildAssetContextsForSync(eq(fieldUser), eq(Set.of(50L))))
                .thenReturn(List.of(context));

        List<SyncAssetDeltaResponse> result = deltaService.buildDeltaRecords(
                fieldUser, List.of(), List.of(workOrder));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocuments()).hasSize(1);
        assertThat(result.get(0).getDocuments().get(0).getDownloadUrl())
                .isEqualTo("/api/operational-documents/900/download")
                .doesNotContain("/tmp/")
                .doesNotContain("stored-file");
    }

    private AssetContextResponse assetContext(Long id, String code, String name) {
        AssetContextSummaryResponse summary = new AssetContextSummaryResponse();
        // use from pattern - AssetContextSummaryResponse has static from
        com.infratrack.asset.Asset asset = assetEntity(id, code, name);
        return new AssetContextResponse(
                AssetContextSummaryResponse.from(asset),
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null);
    }

    private AssetContextResponse assetContextWithDocument(Long id) {
        com.infratrack.asset.Asset asset = assetEntity(id, "PL-001", "Playground");
        com.infratrack.operationaldocument.OperationalDocument document =
                new com.infratrack.operationaldocument.OperationalDocument(
                        asset,
                        com.infratrack.operationaldocument.OperationalDocumentOwnerType.ASSET,
                        id,
                        com.infratrack.operationaldocument.OperationalDocumentType.REPORT,
                        "manual.pdf",
                        "stored-file-key",
                        "application/pdf",
                        1024L,
                        "/tmp/internal-storage-path",
                        null,
                        20L,
                        java.time.LocalDateTime.of(2026, 7, 4, 9, 30));
        document.setId(900L);
        MobileAssetDocumentSummaryResponse documentSummary =
                MobileAssetDocumentSummaryResponse.from(document, "Uploader");

        return new AssetContextResponse(
                AssetContextSummaryResponse.from(asset),
                null,
                null,
                null,
                List.of(documentSummary),
                List.of(),
                List.of(),
                List.of(),
                null);
    }

    private com.infratrack.asset.Asset assetEntity(Long id, String code, String name) {
        com.infratrack.department.Department department = new com.infratrack.department.Department("Parks");
        department.setId(1L);
        com.infratrack.assetcategory.AssetCategory category = new com.infratrack.assetcategory.AssetCategory("Playground");
        category.setId(2L);
        com.infratrack.asset.Asset asset = new com.infratrack.asset.Asset(
                name,
                department,
                category,
                "Memorial Park",
                com.infratrack.asset.AssetStatus.ACTIVE,
                java.time.LocalDate.of(2026, 6, 25),
                10L);
        asset.setId(id);
        return asset;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("field@test.com");
        user.setRole(UserRole.FIELD_EMPLOYEE);
        return user;
    }
}

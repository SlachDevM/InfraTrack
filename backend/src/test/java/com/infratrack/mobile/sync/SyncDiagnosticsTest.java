package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyncDiagnosticsTest {

    @Test
    void recordOperations_countsStatuses() {
        SyncDiagnostics diagnostics = SyncDiagnostics.start();

        SyncOperationResponse accepted = new SyncOperationResponse();
        accepted.setStatus(SyncOperationStatus.ACCEPTED);
        SyncOperationResponse rejected = new SyncOperationResponse();
        rejected.setStatus(SyncOperationStatus.REJECTED);
        SyncOperationResponse ignored = new SyncOperationResponse();
        ignored.setStatus(SyncOperationStatus.IGNORED);
        SyncOperationResponse conflict = new SyncOperationResponse();
        conflict.setStatus(SyncOperationStatus.CONFLICT);

        diagnostics.recordOperations(List.of(accepted, rejected, ignored, conflict));
        diagnostics.recordDeltaInspections(7);
        diagnostics.recordDeltaWorkOrders(3);
        diagnostics.recordDeltaAssets(2);
        diagnostics.recordDashboardIncluded(true);
        diagnostics.recordReferenceDataIncluded(true);
        diagnostics.recordSectionDuration(SyncDeltaSections.WORK_ORDERS, 15);
        diagnostics.recordResponseSizeBytes(2048);

        assertThat(diagnostics.processed()).isEqualTo(4);
        assertThat(diagnostics.accepted()).isEqualTo(1);
        assertThat(diagnostics.rejected()).isEqualTo(1);
        assertThat(diagnostics.ignored()).isEqualTo(1);
        assertThat(diagnostics.conflicts()).isEqualTo(1);
        assertThat(diagnostics.deltaInspections()).isEqualTo(7);
        assertThat(diagnostics.deltaWorkOrders()).isEqualTo(3);
        assertThat(diagnostics.deltaAssets()).isEqualTo(2);
        assertThat(diagnostics.dashboardIncluded()).isTrue();
        assertThat(diagnostics.referenceDataIncluded()).isTrue();
        assertThat(diagnostics.responseSizeBytes()).isEqualTo(2048);
        assertThat(diagnostics.sectionDurationMillis())
                .containsEntry(SyncDeltaSections.WORK_ORDERS, 15L);
        assertThat(diagnostics.elapsedMillis()).isGreaterThanOrEqualTo(0);
    }
}

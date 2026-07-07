package com.infratrack.mobile.sync;

import com.infratrack.inspection.InspectionStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyncReferenceDataLabelsTest {

    @Test
    void inspectionStatuses_useServerAuthoritativeLabels() {
        assertThat(SyncReferenceDataLabels.inspectionStatuses())
                .anyMatch(item -> InspectionStatus.ASSIGNED.name().equals(item.getCode())
                        && "Assigned".equals(item.getLabel()));
    }

    @Test
    void workOrderTypes_useServerAuthoritativeLabels() {
        assertThat(SyncReferenceDataLabels.workOrderTypes())
                .anyMatch(item -> WorkType.INTERNAL_MAINTENANCE.name().equals(item.getCode())
                        && "Internal Maintenance".equals(item.getLabel()));
    }
}

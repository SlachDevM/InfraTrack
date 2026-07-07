package com.infratrack.mobile.sync;

import com.infratrack.asset.AssetStatus;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.mobile.sync.dto.SyncEnumItemResponse;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Server-authoritative display labels for mobile reference data enums (M6.5-BE1).
 * Labels align with the React client constants where equivalent values exist.
 */
final class SyncReferenceDataLabels {

    private static final Map<InspectionStatus, String> INSPECTION_STATUS_LABELS = Map.of(
            InspectionStatus.ASSIGNED, "Assigned",
            InspectionStatus.COMPLETED, "Completed",
            InspectionStatus.CANCELLED, "Cancelled");

    private static final Map<InspectionPriority, String> INSPECTION_PRIORITY_LABELS = Map.of(
            InspectionPriority.LOW, "Low",
            InspectionPriority.NORMAL, "Normal",
            InspectionPriority.HIGH, "High",
            InspectionPriority.URGENT, "Urgent");

    private static final Map<WorkOrderStatus, String> WORK_ORDER_STATUS_LABELS = Map.of(
            WorkOrderStatus.CREATED, "Created",
            WorkOrderStatus.ASSIGNED, "Assigned",
            WorkOrderStatus.COMPLETED, "Completed",
            WorkOrderStatus.CANCELLED, "Cancelled");

    private static final Map<WorkOrderPriority, String> WORK_ORDER_PRIORITY_LABELS = Map.of(
            WorkOrderPriority.LOW, "Low",
            WorkOrderPriority.NORMAL, "Normal",
            WorkOrderPriority.HIGH, "High",
            WorkOrderPriority.URGENT, "Urgent");

    private static final Map<WorkType, String> WORK_ORDER_TYPE_LABELS = Map.of(
            WorkType.INTERNAL_MAINTENANCE, "Internal Maintenance",
            WorkType.CONTRACTOR_WORK, "Contractor Work");

    private static final Map<AssetStatus, String> ASSET_STATUS_LABELS = Map.of(
            AssetStatus.ACTIVE, "Active",
            AssetStatus.LIMITED_SERVICE, "Limited Service",
            AssetStatus.OUT_OF_SERVICE, "Out of Service",
            AssetStatus.DECOMMISSIONED, "Decommissioned");

    private static final Map<IssueSeverity, String> ISSUE_SEVERITY_LABELS = Map.of(
            IssueSeverity.LOW, "Low",
            IssueSeverity.MEDIUM, "Medium",
            IssueSeverity.HIGH, "High",
            IssueSeverity.CRITICAL, "Critical");

    private SyncReferenceDataLabels() {
    }

    static List<SyncEnumItemResponse> inspectionStatuses() {
        return enumItems(InspectionStatus.values(), INSPECTION_STATUS_LABELS);
    }

    static List<SyncEnumItemResponse> inspectionPriorities() {
        return enumItems(InspectionPriority.values(), INSPECTION_PRIORITY_LABELS);
    }

    static List<SyncEnumItemResponse> workOrderStatuses() {
        return enumItems(WorkOrderStatus.values(), WORK_ORDER_STATUS_LABELS);
    }

    static List<SyncEnumItemResponse> workOrderPriorities() {
        return enumItems(WorkOrderPriority.values(), WORK_ORDER_PRIORITY_LABELS);
    }

    static List<SyncEnumItemResponse> workOrderTypes() {
        return enumItems(WorkType.values(), WORK_ORDER_TYPE_LABELS);
    }

    static List<SyncEnumItemResponse> assetStatuses() {
        return enumItems(AssetStatus.values(), ASSET_STATUS_LABELS);
    }

    static List<SyncEnumItemResponse> issueSeverities() {
        return enumItems(IssueSeverity.values(), ISSUE_SEVERITY_LABELS);
    }

    private static <E extends Enum<E>> List<SyncEnumItemResponse> enumItems(
            E[] values,
            Map<E, String> labels) {
        return Arrays.stream(values)
                .map(value -> new SyncEnumItemResponse(value.name(), labels.get(value)))
                .collect(Collectors.toList());
    }
}

package com.infratrack.mobile.sync;

import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.sync.dto.SyncWorkOrderDeltaResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.workorder.WorkOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the work order section of the mobile sync delta (M6.1-BE2).
 */
@Service
class WorkOrderSyncDeltaService {

    private final MobileService mobileService;
    private final MobileAuthorizationService authorizationService;
    private final UserNameLookup userNameLookup;

    WorkOrderSyncDeltaService(
            MobileService mobileService,
            MobileAuthorizationService authorizationService,
            UserNameLookup userNameLookup) {
        this.mobileService = mobileService;
        this.authorizationService = authorizationService;
        this.userNameLookup = userNameLookup;
    }

    @Transactional(readOnly = true)
    List<SyncWorkOrderDeltaResponse> buildDeltaRecords(
            User user, Long updatedSinceMillis, Long updatedUntilMillis) {
        List<WorkOrder> workOrdersForDelta =
                mobileService.listScopedWorkOrdersForSync(user, updatedSinceMillis, updatedUntilMillis);
        Map<Long, String> assignedToNames = resolveAssignedToNames(workOrdersForDelta);

        List<SyncWorkOrderDeltaResponse> workOrderDeltas = new ArrayList<>(workOrdersForDelta.size());
        for (WorkOrder workOrder : workOrdersForDelta) {
            String assignedToName = assignedToNames.get(workOrder.getAssignedToUserId());
            boolean completionEligible = authorizationService.canCompleteMaintenance(user, workOrder);
            workOrderDeltas.add(SyncWorkOrderDeltaResponse.from(
                    workOrder,
                    assignedToName,
                    completionEligible));
        }
        return workOrderDeltas;
    }

    private Map<Long, String> resolveAssignedToNames(List<WorkOrder> workOrders) {
        if (workOrders.isEmpty()) {
            return Map.of();
        }
        Set<Long> userIds = workOrders.stream()
                .map(WorkOrder::getAssignedToUserId)
                .collect(Collectors.toSet());
        return userNameLookup.resolveNames(userIds);
    }
}

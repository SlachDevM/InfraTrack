package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Writes permanent asset history entries for work order lifecycle events.
 */
@Service
public class WorkOrderHistoryRecorder {

    private final AssetHistoryEventRepository assetHistoryEventRepository;

    public WorkOrderHistoryRecorder(AssetHistoryEventRepository assetHistoryEventRepository) {
        this.assetHistoryEventRepository = assetHistoryEventRepository;
    }

    public void recordWorkOrderCreated(Asset asset, Long userId, LocalDate eventDate) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.WORK_ORDER_CREATED,
                userId,
                eventDate
        ));
    }

    public void recordWorkOrderAssigned(Asset asset, Long userId, LocalDate eventDate) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.WORK_ORDER_ASSIGNED,
                userId,
                eventDate
        ));
    }
}

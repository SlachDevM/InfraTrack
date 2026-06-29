package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Writes permanent asset history entries for inspection lifecycle events.
 */
@Service
public class InspectionHistoryRecorder {

    private final AssetHistoryEventRepository assetHistoryEventRepository;

    public InspectionHistoryRecorder(AssetHistoryEventRepository assetHistoryEventRepository) {
        this.assetHistoryEventRepository = assetHistoryEventRepository;
    }

    public void recordInspectionAssigned(Asset asset, Long userId, LocalDate eventDate) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.INSPECTION_ASSIGNED,
                userId,
                eventDate
        ));
    }

    public void recordInspectionCompleted(Asset asset, Long userId, LocalDate eventDate) {
        recordInspectionCompleted(asset, userId, eventDate, null);
    }

    public void recordInspectionCompleted(Asset asset, Long userId, LocalDate eventDate, String details) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.INSPECTION_COMPLETED,
                userId,
                eventDate,
                details
        ));
    }

    public void recordPreventiveInspectionCreated(
            Asset asset,
            Long userId,
            LocalDate eventDate,
            Long candidateId,
            String planCodeSnapshot) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.PREVENTIVE_INSPECTION_CREATED,
                userId,
                eventDate,
                "Preventive inspection created from candidate "
                        + candidateId
                        + " for plan "
                        + planCodeSnapshot
                        + "."));
    }
}

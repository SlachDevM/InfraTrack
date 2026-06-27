package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.INSPECTION_COMPLETED,
                userId,
                eventDate
        ));
    }
}

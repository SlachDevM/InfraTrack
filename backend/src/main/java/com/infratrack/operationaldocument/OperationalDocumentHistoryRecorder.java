package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OperationalDocumentHistoryRecorder {

    private final AssetHistoryEventRepository assetHistoryEventRepository;

    public OperationalDocumentHistoryRecorder(AssetHistoryEventRepository assetHistoryEventRepository) {
        this.assetHistoryEventRepository = assetHistoryEventRepository;
    }

    public void recordUploaded(Asset asset, Long userId, LocalDate eventDate) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.OPERATIONAL_DOCUMENT_UPLOADED,
                userId,
                eventDate
        ));
    }
}

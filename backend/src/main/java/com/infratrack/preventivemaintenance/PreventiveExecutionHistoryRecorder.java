package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Writes permanent asset history entries for preventive execution candidate lifecycle events.
 */
@Service
public class PreventiveExecutionHistoryRecorder {

    private final AssetHistoryEventRepository assetHistoryEventRepository;

    public PreventiveExecutionHistoryRecorder(AssetHistoryEventRepository assetHistoryEventRepository) {
        this.assetHistoryEventRepository = assetHistoryEventRepository;
    }

    public void recordCandidateGenerated(Asset asset, Long userId, String planCodeSnapshot) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.PREVENTIVE_CANDIDATE_GENERATED,
                userId,
                LocalDate.now(),
                "Preventive candidate generated for plan " + planCodeSnapshot + "."));
    }

    public void recordCandidateApproved(Asset asset, Long userId, String planCodeSnapshot) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.PREVENTIVE_CANDIDATE_APPROVED,
                userId,
                LocalDate.now(),
                "Preventive candidate approved for plan " + planCodeSnapshot + "."));
    }

    public void recordCandidateRejected(Asset asset, Long userId, String planCodeSnapshot) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.PREVENTIVE_CANDIDATE_REJECTED,
                userId,
                LocalDate.now(),
                "Preventive candidate rejected for plan " + planCodeSnapshot + "."));
    }

    public void recordCandidateDismissed(Asset asset, Long userId, String planCodeSnapshot) {
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.PREVENTIVE_CANDIDATE_DISMISSED,
                userId,
                LocalDate.now(),
                "Preventive candidate dismissed for plan " + planCodeSnapshot + "."));
    }
}

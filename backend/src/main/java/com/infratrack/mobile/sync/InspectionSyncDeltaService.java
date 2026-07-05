package com.infratrack.mobile.sync;

import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the inspection section of the mobile sync delta (M5.4-BE).
 */
@Service
class InspectionSyncDeltaService {

    private static final String INVALID_TOKEN_MESSAGE =
            "Sync token is invalid; returning full inspection delta.";

    private final MobileService mobileService;
    private final InspectionAnswerRepository inspectionAnswerRepository;
    private final UserNameLookup userNameLookup;

    InspectionSyncDeltaService(
            MobileService mobileService,
            InspectionAnswerRepository inspectionAnswerRepository,
            UserNameLookup userNameLookup) {
        this.mobileService = mobileService;
        this.inspectionAnswerRepository = inspectionAnswerRepository;
        this.userNameLookup = userNameLookup;
    }

    @Transactional(readOnly = true)
    SyncDeltaBuildResult build(User user, String previousSyncToken) {
        List<SyncWarningResponse> warnings = new ArrayList<>();
        List<Inspection> scoped = mobileService.listScopedInspectionsForSync(user);

        Optional<SyncToken> previousToken = SyncToken.tryFromOpaqueValue(previousSyncToken);
        List<Inspection> inspectionsForDelta;
        if (previousSyncToken == null || previousSyncToken.isBlank()) {
            inspectionsForDelta = scoped;
        } else if (previousToken.isEmpty()) {
            inspectionsForDelta = scoped;
            warnings.add(new SyncWarningResponse(SyncWarningCode.FULL_SYNC_REQUIRED, INVALID_TOKEN_MESSAGE));
        } else if (previousToken.get().getVersion() != SyncProtocolVersion.CURRENT) {
            inspectionsForDelta = scoped;
            warnings.add(new SyncWarningResponse(
                    SyncWarningCode.FULL_SYNC_REQUIRED,
                    "Sync token protocol version is unsupported; returning full inspection delta."));
        } else {
            long sinceMillis = previousToken.get().getIssuedAt().toEpochMilli();
            inspectionsForDelta = scoped.stream()
                    .filter(inspection -> inspection.getUpdatedAt() >= sinceMillis)
                    .toList();
        }

        Map<Long, String> assignedToNames = resolveAssignedToNames(inspectionsForDelta);
        List<SyncInspectionDeltaResponse> inspectionDeltas = new ArrayList<>(inspectionsForDelta.size());
        for (Inspection inspection : inspectionsForDelta) {
            List<InspectionAnswer> answers = inspectionAnswerRepository
                    .findByInspectionIdOrderByQuestionDisplayOrder(inspection.getId());
            String assignedToName = assignedToNames.get(inspection.getAssignedToUserId());
            inspectionDeltas.add(SyncInspectionDeltaResponse.from(inspection, answers, assignedToName));
        }

        SyncDeltaResponse delta = SyncDeltaResponse.empty();
        delta.setInspections(inspectionDeltas);
        return new SyncDeltaBuildResult(delta, warnings);
    }

    private Map<Long, String> resolveAssignedToNames(List<Inspection> inspections) {
        Set<Long> userIds = inspections.stream()
                .map(Inspection::getAssignedToUserId)
                .collect(Collectors.toSet());
        return userNameLookup.resolveNames(userIds);
    }

    record SyncDeltaBuildResult(SyncDeltaResponse delta, List<SyncWarningResponse> warnings) {
    }
}

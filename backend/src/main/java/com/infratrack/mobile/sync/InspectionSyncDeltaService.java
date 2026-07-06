package com.infratrack.mobile.sync;

import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.mobile.MobileInspectionChecklistLoader;
import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionQuestionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionTemplateDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final MobileInspectionChecklistLoader checklistLoader;

    InspectionSyncDeltaService(
            MobileService mobileService,
            InspectionAnswerRepository inspectionAnswerRepository,
            UserNameLookup userNameLookup,
            MobileInspectionChecklistLoader checklistLoader) {
        this.mobileService = mobileService;
        this.inspectionAnswerRepository = inspectionAnswerRepository;
        this.userNameLookup = userNameLookup;
        this.checklistLoader = checklistLoader;
    }

    @Transactional(readOnly = true)
    SyncDeltaBuildResult build(User user, String previousSyncToken) {
        List<SyncWarningResponse> warnings = new ArrayList<>();
        Optional<SyncToken> previousToken = SyncToken.tryFromOpaqueValue(previousSyncToken);
        Long updatedSinceMillis = resolveUpdatedSinceMillis(previousSyncToken, previousToken, warnings);

        List<Inspection> inspectionsForDelta =
                mobileService.listScopedInspectionsForSync(user, updatedSinceMillis);

        Map<Long, List<InspectionAnswer>> answersByInspectionId = loadAnswersByInspectionId(inspectionsForDelta);
        Map<Long, String> assignedToNames = resolveAssignedToNames(inspectionsForDelta);
        Set<Long> templateIds = resolveTemplateIds(inspectionsForDelta);
        MobileInspectionChecklistLoader.ChecklistPayload checklistPayload =
                checklistLoader.loadChecklistPayloadByTemplateIds(templateIds);
        Map<Long, List<SyncInspectionQuestionDeltaResponse>> questionsByTemplateId =
                checklistPayload.questionsByTemplateId();
        Map<Long, Map<String, Long>> choiceIdByQuestionAndCode =
                checklistPayload.choiceIdByQuestionAndCode();

        List<SyncInspectionDeltaResponse> inspectionDeltas = new ArrayList<>(inspectionsForDelta.size());
        for (Inspection inspection : inspectionsForDelta) {
            List<InspectionAnswer> answers = answersByInspectionId.getOrDefault(
                    inspection.getId(), Collections.emptyList());
            String assignedToName = assignedToNames.get(inspection.getAssignedToUserId());
            InspectionTemplate inspectionTemplate = inspection.getInspectionTemplate();
            SyncInspectionTemplateDeltaResponse template = inspectionTemplate != null
                    ? SyncInspectionTemplateDeltaResponse.from(inspectionTemplate)
                    : null;
            List<SyncInspectionQuestionDeltaResponse> questions = inspectionTemplate != null
                    ? questionsByTemplateId.getOrDefault(inspectionTemplate.getId(), List.of())
                    : List.of();
            inspectionDeltas.add(SyncInspectionDeltaResponse.from(
                    inspection,
                    answers,
                    assignedToName,
                    template,
                    questions,
                    choiceIdByQuestionAndCode));
        }

        SyncDeltaResponse delta = SyncDeltaResponse.empty();
        delta.setInspections(inspectionDeltas);
        return new SyncDeltaBuildResult(delta, warnings);
    }

    private Set<Long> resolveTemplateIds(List<Inspection> inspections) {
        return inspections.stream()
                .map(Inspection::getInspectionTemplate)
                .filter(Objects::nonNull)
                .map(InspectionTemplate::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Long resolveUpdatedSinceMillis(
            String previousSyncToken,
            Optional<SyncToken> previousToken,
            List<SyncWarningResponse> warnings) {
        if (previousSyncToken == null || previousSyncToken.isBlank()) {
            return null;
        }
        if (previousToken.isEmpty()) {
            warnings.add(new SyncWarningResponse(SyncWarningCode.FULL_SYNC_REQUIRED, INVALID_TOKEN_MESSAGE));
            return null;
        }
        if (previousToken.get().getVersion() != SyncProtocolVersion.CURRENT) {
            warnings.add(new SyncWarningResponse(
                    SyncWarningCode.FULL_SYNC_REQUIRED,
                    "Sync token protocol version is unsupported; returning full inspection delta."));
            return null;
        }
        return previousToken.get().getIssuedAt().toEpochMilli();
    }

    private Map<Long, List<InspectionAnswer>> loadAnswersByInspectionId(List<Inspection> inspections) {
        if (inspections.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> inspectionIds = inspections.stream().map(Inspection::getId).toList();
        return inspectionAnswerRepository.findByInspectionIdInOrderByQuestionDisplayOrder(inspectionIds)
                .stream()
                .collect(Collectors.groupingBy(answer -> answer.getInspection().getId()));
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

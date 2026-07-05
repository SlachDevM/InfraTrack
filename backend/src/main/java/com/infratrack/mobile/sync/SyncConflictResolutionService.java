package com.infratrack.mobile.sync;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionAction;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionResponse;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionStatus;
import com.infratrack.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;

/**
 * Records explicit conflict resolution decisions for mobile sync (M5.5-BE2).
 * Stateless — does not apply client payloads or mutate workflow state.
 */
@Service
public class SyncConflictResolutionService {

    private static final Logger log = LoggerFactory.getLogger(SyncConflictResolutionService.class);

    static final String OPERATION_TYPE = "SAVE_INSPECTION_PROGRESS";
    static final String ENTITY_TYPE = "INSPECTION";

    private static final String UNSUPPORTED_SCOPE_MESSAGE =
            "Conflict resolution is not supported for this operation.";
    private static final String RESOLVED_SERVER_WINS_MESSAGE =
            "Conflict resolved. Server state is authoritative.";
    private static final String RESOLVED_DISCARD_MESSAGE =
            "Conflict resolved. Client changes discarded.";
    private static final String RETRY_REQUIRED_MESSAGE =
            "Refresh from server and retry if changes are still required.";
    private static final String MANUAL_REVIEW_MESSAGE =
            "Conflict requires manual review before proceeding.";
    private static final String UNAUTHORIZED_MESSAGE =
            "You are not authorized to resolve this conflict.";

    private final MobileAuthorizationService authorizationService;
    private final InspectionRepository inspectionRepository;
    private final Clock clock;
    private final SyncConflictResolutionMetricsRecorder metricsRecorder;

    public SyncConflictResolutionService(
            MobileAuthorizationService authorizationService,
            InspectionRepository inspectionRepository,
            Clock clock,
            SyncConflictResolutionMetricsRecorder metricsRecorder) {
        this.authorizationService = authorizationService;
        this.inspectionRepository = inspectionRepository;
        this.clock = clock;
        this.metricsRecorder = metricsRecorder;
    }

    public SyncConflictResolutionResponse resolve(Long userId, SyncConflictResolutionRequest request) {
        User user = authorizationService.requireMobileUser(userId);

        if (!isSupportedScope(request)) {
            SyncConflictResolutionResponse response = buildResponse(
                    request,
                    SyncConflictResolutionStatus.REJECTED,
                    UNSUPPORTED_SCOPE_MESSAGE);
            recordOutcome(userId, response);
            return response;
        }

        Optional<Inspection> inspection = inspectionRepository.findById(request.getEntityId());
        if (inspection.isPresent()) {
            try {
                authorizationService.requireCanViewInspectionBundle(user, inspection.get());
            } catch (ForbiddenOperationException ex) {
                SyncConflictResolutionResponse response = buildResponse(
                        request,
                        SyncConflictResolutionStatus.MANUAL_REVIEW_REQUIRED,
                        UNAUTHORIZED_MESSAGE);
                recordOutcome(userId, response);
                return response;
            }
        }

        SyncConflictResolutionResponse response = mapResolution(request);
        recordOutcome(userId, response);
        return response;
    }

    private SyncConflictResolutionResponse mapResolution(SyncConflictResolutionRequest request) {
        return switch (request.getResolution()) {
            case SERVER_WINS -> buildResponse(
                    request,
                    SyncConflictResolutionStatus.RESOLVED,
                    RESOLVED_SERVER_WINS_MESSAGE);
            case DISCARD_CLIENT -> buildResponse(
                    request,
                    SyncConflictResolutionStatus.RESOLVED,
                    RESOLVED_DISCARD_MESSAGE);
            case CLIENT_RETRY -> buildResponse(
                    request,
                    SyncConflictResolutionStatus.RETRY_REQUIRED,
                    RETRY_REQUIRED_MESSAGE);
            case MANUAL_REVIEW -> buildResponse(
                    request,
                    SyncConflictResolutionStatus.MANUAL_REVIEW_REQUIRED,
                    MANUAL_REVIEW_MESSAGE);
        };
    }

    private boolean isSupportedScope(SyncConflictResolutionRequest request) {
        return OPERATION_TYPE.equals(request.getOperationType())
                && ENTITY_TYPE.equals(request.getEntityType());
    }

    private SyncConflictResolutionResponse buildResponse(
            SyncConflictResolutionRequest request,
            SyncConflictResolutionStatus status,
            String message) {
        SyncConflictResolutionResponse response = new SyncConflictResolutionResponse();
        response.setOperationId(request.getOperationId());
        response.setEntityType(request.getEntityType());
        response.setEntityId(request.getEntityId());
        response.setOperationType(request.getOperationType());
        response.setResolution(request.getResolution());
        response.setStatus(status);
        response.setMessage(message);
        response.setServerTime(clock.instant());
        return response;
    }

    private void recordOutcome(Long userId, SyncConflictResolutionResponse response) {
        metricsRecorder.record(response.getStatus());
        log.info(
                "Conflict resolution completed user={} operationId={} resolution={} status={}",
                userId,
                response.getOperationId(),
                response.getResolution(),
                response.getStatus());
    }
}

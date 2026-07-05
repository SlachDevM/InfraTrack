package com.infratrack.mobile.sync;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionAction;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionResponse;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionStatus;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncConflictResolutionServiceTest {

    private static final Long USER_ID = 20L;
    private static final Long INSPECTION_ID = 123L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T09:00:00Z");

    @Mock
    private MobileAuthorizationService authorizationService;

    @Mock
    private InspectionRepository inspectionRepository;

    private SyncConflictResolutionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        service = new SyncConflictResolutionService(
                authorizationService,
                inspectionRepository,
                clock,
                new SyncConflictResolutionMetricsRecorder(new SimpleMeterRegistry()));
    }

    @Test
    void resolve_serverWins_doesNotMutateServerState() {
        User user = fieldUser();
        Inspection inspection = inspection();
        when(authorizationService.requireMobileUser(USER_ID)).thenReturn(user);
        when(inspectionRepository.findById(INSPECTION_ID)).thenReturn(Optional.of(inspection));

        SyncConflictResolutionResponse response = service.resolve(USER_ID, request(SyncConflictResolutionAction.SERVER_WINS));

        assertThat(response.getStatus()).isEqualTo(SyncConflictResolutionStatus.RESOLVED);
        assertThat(response.getResolution()).isEqualTo(SyncConflictResolutionAction.SERVER_WINS);
        assertThat(response.getServerTime()).isEqualTo(FIXED_INSTANT);
        verify(authorizationService).requireCanViewInspectionBundle(user, inspection);
    }

    @Test
    void resolve_discardClient_behavesLikeServerWins() {
        User user = fieldUser();
        when(authorizationService.requireMobileUser(USER_ID)).thenReturn(user);
        when(inspectionRepository.findById(INSPECTION_ID)).thenReturn(Optional.of(inspection()));

        SyncConflictResolutionResponse response = service.resolve(USER_ID, request(SyncConflictResolutionAction.DISCARD_CLIENT));

        assertThat(response.getStatus()).isEqualTo(SyncConflictResolutionStatus.RESOLVED);
        assertThat(response.getResolution()).isEqualTo(SyncConflictResolutionAction.DISCARD_CLIENT);
    }

    @Test
    void resolve_clientRetry_returnsRetryRequiredWithoutMutation() {
        User user = fieldUser();
        when(authorizationService.requireMobileUser(USER_ID)).thenReturn(user);
        when(inspectionRepository.findById(INSPECTION_ID)).thenReturn(Optional.of(inspection()));

        SyncConflictResolutionResponse response = service.resolve(USER_ID, request(SyncConflictResolutionAction.CLIENT_RETRY));

        assertThat(response.getStatus()).isEqualTo(SyncConflictResolutionStatus.RETRY_REQUIRED);
    }

    @Test
    void resolve_manualReview_returnsManualReviewRequiredWithoutMutation() {
        User user = fieldUser();
        when(authorizationService.requireMobileUser(USER_ID)).thenReturn(user);
        when(inspectionRepository.findById(INSPECTION_ID)).thenReturn(Optional.of(inspection()));

        SyncConflictResolutionResponse response = service.resolve(USER_ID, request(SyncConflictResolutionAction.MANUAL_REVIEW));

        assertThat(response.getStatus()).isEqualTo(SyncConflictResolutionStatus.MANUAL_REVIEW_REQUIRED);
    }

    @Test
    void resolve_unsupportedOperation_returnsRejected() {
        when(authorizationService.requireMobileUser(USER_ID)).thenReturn(fieldUser());

        SyncConflictResolutionRequest request = request(SyncConflictResolutionAction.SERVER_WINS);
        request.setOperationType("COMPLETE_INSPECTION");

        SyncConflictResolutionResponse response = service.resolve(USER_ID, request);

        assertThat(response.getStatus()).isEqualTo(SyncConflictResolutionStatus.REJECTED);
        verify(inspectionRepository, never()).findById(any());
    }

    @Test
    void resolve_forbiddenInspection_returnsManualReviewRequired() {
        User user = fieldUser();
        Inspection inspection = inspection();
        when(authorizationService.requireMobileUser(USER_ID)).thenReturn(user);
        when(inspectionRepository.findById(INSPECTION_ID)).thenReturn(Optional.of(inspection));
        org.mockito.Mockito.doThrow(new ForbiddenOperationException("You may only access inspections assigned to you."))
                .when(authorizationService)
                .requireCanViewInspectionBundle(eq(user), eq(inspection));

        SyncConflictResolutionResponse response = service.resolve(USER_ID, request(SyncConflictResolutionAction.SERVER_WINS));

        assertThat(response.getStatus()).isEqualTo(SyncConflictResolutionStatus.MANUAL_REVIEW_REQUIRED);
    }

    @Test
    void resolve_unknownInspection_allowsServerWinsWithoutViewCheck() {
        when(authorizationService.requireMobileUser(USER_ID)).thenReturn(fieldUser());
        when(inspectionRepository.findById(INSPECTION_ID)).thenReturn(Optional.empty());

        SyncConflictResolutionResponse response = service.resolve(USER_ID, request(SyncConflictResolutionAction.SERVER_WINS));

        assertThat(response.getStatus()).isEqualTo(SyncConflictResolutionStatus.RESOLVED);
        verify(authorizationService, never()).requireCanViewInspectionBundle(any(), any());
    }

    private static SyncConflictResolutionRequest request(SyncConflictResolutionAction resolution) {
        SyncConflictResolutionRequest request = new SyncConflictResolutionRequest();
        request.setOperationId("op-1");
        request.setEntityType("INSPECTION");
        request.setEntityId(INSPECTION_ID);
        request.setOperationType("SAVE_INSPECTION_PROGRESS");
        request.setConflictType(SyncConflictType.WORKFLOW_COMPLETED);
        request.setResolution(resolution);
        return request;
    }

    private static User fieldUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("field@test.com");
        user.setRole(UserRole.FIELD_EMPLOYEE);
        return user;
    }

    private static Inspection inspection() {
        return org.mockito.Mockito.mock(Inspection.class);
    }
}

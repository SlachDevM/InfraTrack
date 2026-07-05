package com.infratrack.mobile.sync;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileSyncServiceTest {

    private static final Long FIELD_USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T08:30:00Z");

    @Mock
    private MobileAuthorizationService authorizationService;

    private MobileSyncService mobileSyncService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        mobileSyncService = new MobileSyncService(
                authorizationService,
                clock,
                new DefaultSyncTokenService(clock),
                new NoOpSyncOperationProcessor(),
                new NoOpSyncConflictResolver());
    }

    @Test
    void sync_validRequest_returnsEmptyResponse() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncRequest request = validRequest();
        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getServerTime()).isEqualTo(FIXED_INSTANT);
        assertThat(response.getProtocolVersion()).isEqualTo(SyncProtocolVersion.CURRENT);
        assertThat(response.getNextSyncToken()).isNotBlank();
        assertThat(response.getDelta().getAssets()).isEmpty();
        assertThat(response.getDelta().getInspections()).isEmpty();
        assertThat(response.getDelta().getWorkOrders()).isEmpty();
        assertThat(response.getDelta().getDocuments()).isEmpty();
        assertThat(response.getDelta().getUsers()).isEmpty();
        assertThat(response.getDelta().getReferenceData()).isEmpty();
        assertThat(response.getOperations()).isEmpty();
        assertThat(response.getConflicts()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();
        assertThat(response.isRequiresFullSync()).isFalse();
        verify(authorizationService).requireMobileUser(FIELD_USER_ID);
    }

    @Test
    void sync_acceptsPendingOperationsWithoutProcessing() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        PendingOperationRequest pending = new PendingOperationRequest();
        pending.setOperationId("op-1");
        pending.setEntityType("INSPECTION");
        pending.setEntityId(42L);
        pending.setOperationType("SAVE_INSPECTION_PROGRESS");
        pending.setPayload("{\"answers\":[]}");

        SyncRequest request = validRequest();
        request.setPendingOperations(List.of(pending));

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getOperations()).isEmpty();
        assertThat(response.getConflicts()).isEmpty();
    }

    @Test
    void sync_operationalCoordinator_isRejected() {
        doThrow(new ForbiddenOperationException("Mobile API access is not available for this role."))
                .when(authorizationService).requireMobileUser(FIELD_USER_ID);

        assertThatThrownBy(() -> mobileSyncService.sync(FIELD_USER_ID, validRequest()))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Mobile API access is not available for this role.");
    }

    private SyncRequest validRequest() {
        SyncRequest request = new SyncRequest();
        request.setClientId("android-install-uuid");
        request.setClientVersion("1");
        request.setAppVersion("1.1.0");
        return request;
    }

    private User user(UserRole role) {
        User user = new User();
        user.setId(FIELD_USER_ID);
        user.setEmail("field@test.com");
        user.setRole(role);
        return user;
    }
}

package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.notification.NotificationService;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceNotificationTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private BusinessTriggerRepository businessTriggerRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    private InspectionService inspectionService;

    @BeforeEach
    void setUp() {
        OperationalEventNotificationService operationalEventNotificationService =
                new OperationalEventNotificationService(notificationService, userRepository);
        inspectionService = new InspectionService(
                inspectionRepository,
                businessTriggerRepository,
                assetHistoryEventRepository,
                userService,
                operationalEventNotificationService);
    }

    @Test
    void assignInspection_shouldNotRollbackWhenNotificationFails() {
        AssignInspectionRequest request = new AssignInspectionRequest();
        request.setBusinessTriggerId(1L);
        request.setAssignedToUserId(20L);
        request.setExpectedCompletionDate(LocalDate.now().plusDays(7));

        BusinessTrigger trigger = businessTrigger();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> {
            Inspection inspection = invocation.getArgument(0);
            inspection.setId(100L);
            return inspection;
        });
        when(notificationService.create(anyLong(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("notification failed"));

        var response = inspectionService.assignInspection(request, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        verify(inspectionRepository).save(any(Inspection.class));
        verify(assetHistoryEventRepository).save(any());
        verify(notificationService).create(
                eq(20L),
                eq(OperationalEventNotificationService.INSPECTION_ASSIGNED_TITLE),
                eq(OperationalEventNotificationService.INSPECTION_ASSIGNED_MESSAGE),
                eq(OperationalEventNotificationService.INSPECTIONS_ROUTE));
    }

    private BusinessTrigger businessTrigger() {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L);
        asset.setId(5L);
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
                false,
                10L);
        trigger.setId(1L);
        return trigger;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}

package com.infratrack.businesstrigger;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.dto.CreateBusinessTriggerRequest;
import com.infratrack.department.Department;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessTriggerServiceTest {

    @Mock
    private BusinessTriggerRepository businessTriggerRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BusinessTriggerService businessTriggerService;

    @Test
    void createBusinessTrigger_shouldCreateTriggerAndHistoryEvent_whenValid() {
        CreateBusinessTriggerRequest request = validRequest();
        Asset asset = asset(1L, "Central Playground");
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(businessTriggerRepository.save(any(BusinessTrigger.class))).thenAnswer(invocation -> {
            BusinessTrigger trigger = invocation.getArgument(0);
            trigger.setId(100L);
            return trigger;
        });

        var response = businessTriggerService.createBusinessTrigger(request, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getAssetId()).isEqualTo(1L);
        assertThat(response.getAssetName()).isEqualTo("Central Playground");
        assertThat(response.getType()).isEqualTo(BusinessTriggerType.CUSTOMER_REQUEST);
        assertThat(response.getReason()).isEqualTo("Damaged equipment reported by resident");
        assertThat(response.isUrgent()).isFalse();

        ArgumentCaptor<BusinessTrigger> triggerCaptor = ArgumentCaptor.forClass(BusinessTrigger.class);
        verify(businessTriggerRepository, times(1)).save(triggerCaptor.capture());
        assertThat(triggerCaptor.getValue().getAsset().getId()).isEqualTo(1L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository, times(1)).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.BUSINESS_TRIGGER_CREATED);
        assertThat(historyCaptor.getValue().getAsset().getId()).isEqualTo(1L);
        assertThat(historyCaptor.getValue().getPerformedByUserId()).isEqualTo(10L);
        assertThat(historyCaptor.getValue().getEventDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createBusinessTrigger_shouldMarkEmergencyAsUrgent() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setType(BusinessTriggerType.EMERGENCY_EVENT);
        request.setUrgent(false);

        Asset asset = asset(1L, "Central Playground");
        User manager = user(10L, UserRole.MANAGER);

        when(userService.getById(10L)).thenReturn(manager);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(businessTriggerRepository.save(any(BusinessTrigger.class))).thenAnswer(invocation -> {
            BusinessTrigger trigger = invocation.getArgument(0);
            trigger.setId(100L);
            return trigger;
        });

        var response = businessTriggerService.createBusinessTrigger(request, 10L);

        assertThat(response.isUrgent()).isTrue();
        verify(businessTriggerRepository).save(argThat(trigger -> trigger.isUrgent()));
    }

    @Test
    void createBusinessTrigger_shouldRejectMissingAssetId() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setAssetId(null);
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBusinessTrigger_shouldRejectInvalidAsset() {
        CreateBusinessTriggerRequest request = validRequest();
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);
        when(assetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBusinessTrigger_shouldRejectMissingType() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setType(null);
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBusinessTrigger_shouldRejectBlankReason() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setReason("  ");
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createBusinessTrigger_shouldRejectAdministrator() {
        CreateBusinessTriggerRequest request = validRequest();
        User administrator = user(10L, UserRole.ADMINISTRATOR);
        when(userService.getById(10L)).thenReturn(administrator);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);

        verify(businessTriggerRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void createBusinessTrigger_shouldRejectFieldEmployee() {
        CreateBusinessTriggerRequest request = validRequest();
        User fieldEmployee = user(10L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(10L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void createBusinessTrigger_shouldRejectContractor() {
        CreateBusinessTriggerRequest request = validRequest();
        User contractor = user(10L, UserRole.CONTRACTOR);
        when(userService.getById(10L)).thenReturn(contractor);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    private CreateBusinessTriggerRequest validRequest() {
        CreateBusinessTriggerRequest request = new CreateBusinessTriggerRequest();
        request.setAssetId(1L);
        request.setType(BusinessTriggerType.CUSTOMER_REQUEST);
        request.setReason("Damaged equipment reported by resident");
        request.setUrgent(false);
        return request;
    }

    private Asset asset(Long id, String name) {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                name,
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L
        );
        asset.setId(id);
        return asset;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}

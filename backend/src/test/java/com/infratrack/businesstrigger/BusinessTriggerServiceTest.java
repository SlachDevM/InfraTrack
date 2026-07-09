package com.infratrack.businesstrigger;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.dto.CreateBusinessTriggerRequest;
import com.infratrack.businesstrigger.dto.BusinessTriggerResponse;
import com.infratrack.department.Department;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.inspection.InspectionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
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
        User coordinator = userWithDepartment(10L, UserRole.OPERATIONAL_COORDINATOR, 1L);

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
        User manager = userWithDepartment(10L, UserRole.MANAGER, 1L);

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
        User manager = userWithDepartment(10L, UserRole.MANAGER, 1L);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createBusinessTrigger_shouldRejectInvalidAsset() {
        CreateBusinessTriggerRequest request = validRequest();
        User manager = userWithDepartment(10L, UserRole.MANAGER, 1L);
        when(userService.getById(10L)).thenReturn(manager);
        when(assetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createBusinessTrigger_shouldRejectMissingType() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setType(null);
        User manager = userWithDepartment(10L, UserRole.MANAGER, 1L);
        when(userService.getById(10L)).thenReturn(manager);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(1L, "Central Playground")));

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createBusinessTrigger_shouldRejectBlankReason() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setReason("  ");
        User manager = userWithDepartment(10L, UserRole.MANAGER, 1L);
        when(userService.getById(10L)).thenReturn(manager);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset(1L, "Central Playground")));

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createBusinessTrigger_shouldRejectAdministrator() {
        CreateBusinessTriggerRequest request = validRequest();
        User administrator = user(10L, UserRole.ADMINISTRATOR);
        when(userService.getById(10L)).thenReturn(administrator);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(businessTriggerRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void createBusinessTrigger_shouldRejectFieldEmployee() {
        CreateBusinessTriggerRequest request = validRequest();
        User fieldEmployee = user(10L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(10L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createBusinessTrigger_shouldRejectContractor() {
        CreateBusinessTriggerRequest request = validRequest();
        User contractor = user(10L, UserRole.CONTRACTOR);
        when(userService.getById(10L)).thenReturn(contractor);

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createBusinessTrigger_shouldAllowManagerForOwnDepartmentAsset() {
        CreateBusinessTriggerRequest request = validRequest();
        Asset asset = asset(1L, "Central Playground");
        User manager = userWithDepartment(10L, UserRole.MANAGER, 1L);

        when(userService.getById(10L)).thenReturn(manager);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(businessTriggerRepository.save(any(BusinessTrigger.class))).thenAnswer(invocation -> {
            BusinessTrigger trigger = invocation.getArgument(0);
            trigger.setId(100L);
            return trigger;
        });

        var response = businessTriggerService.createBusinessTrigger(request, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        verify(businessTriggerRepository).save(any(BusinessTrigger.class));
    }

    @Test
    void createBusinessTrigger_shouldRejectManagerForOtherDepartmentAsset() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setAssetId(2L);
        User manager = userWithDepartment(10L, UserRole.MANAGER, 1L);
        Asset asset = asset(2L, "Other Playground");
        asset.getDepartment().setId(2L);

        when(userService.getById(10L)).thenReturn(manager);
        when(assetRepository.findById(2L)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only create business triggers for assets in your own department.");

        verify(businessTriggerRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void createBusinessTrigger_shouldAllowCoordinatorForOwnDepartmentAsset() {
        CreateBusinessTriggerRequest request = validRequest();
        Asset asset = asset(1L, "Central Playground");
        User coordinator = userWithDepartment(10L, UserRole.OPERATIONAL_COORDINATOR, 1L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(businessTriggerRepository.save(any(BusinessTrigger.class))).thenAnswer(invocation -> {
            BusinessTrigger trigger = invocation.getArgument(0);
            trigger.setId(100L);
            return trigger;
        });

        var response = businessTriggerService.createBusinessTrigger(request, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        verify(businessTriggerRepository).save(any(BusinessTrigger.class));
    }

    @Test
    void createBusinessTrigger_shouldRejectCoordinatorForOtherDepartmentAsset() {
        CreateBusinessTriggerRequest request = validRequest();
        request.setAssetId(2L);
        User coordinator = userWithDepartment(10L, UserRole.OPERATIONAL_COORDINATOR, 1L);
        Asset asset = asset(2L, "Other Playground");
        asset.getDepartment().setId(2L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(2L)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> businessTriggerService.createBusinessTrigger(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only create business triggers for assets in your own department.");

        verify(businessTriggerRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void listPage_shouldReturnPagedTriggers() {
        BusinessTrigger trigger = businessTrigger(100L);
        Pageable pageable = PageRequest.of(1, 10);
        when(businessTriggerRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(trigger), pageable, 25));

        Page<BusinessTriggerResponse> page = businessTriggerService.listPage(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(100L);
        assertThat(page.getTotalPages()).isEqualTo(3);
        verify(businessTriggerRepository).findAllByOrderByCreatedAtDesc(pageable);
        verify(businessTriggerRepository, never()).findEligibleForInspectionOrderByCreatedAtDesc(
                any(), any());
    }

    @Test
    void listPage_shouldReturnEligibleTriggersOnly_whenEligibleForInspectionRequested() {
        BusinessTrigger trigger = businessTrigger(200L);
        Pageable pageable = PageRequest.of(0, 20);
        when(businessTriggerRepository.findEligibleForInspectionOrderByCreatedAtDesc(
                        InspectionStatus.ASSIGNED, pageable))
                .thenReturn(new PageImpl<>(List.of(trigger), pageable, 1));

        Page<BusinessTriggerResponse> page = businessTriggerService.listPage(pageable, true);

        assertThat(page.getContent()).extracting(BusinessTriggerResponse::getId).containsExactly(200L);
        verify(businessTriggerRepository).findEligibleForInspectionOrderByCreatedAtDesc(
                InspectionStatus.ASSIGNED, pageable);
        verify(businessTriggerRepository, never()).findAllByOrderByCreatedAtDesc(any());
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

    private User userWithDepartment(Long id, UserRole role, Long departmentId) {
        User user = user(id, role);
        Department department = new Department("Parks");
        department.setId(departmentId);
        user.setDepartment(department);
        return user;
    }

    private BusinessTrigger businessTrigger(Long id) {
        BusinessTrigger trigger = new BusinessTrigger(
                asset(1L, "Central Playground"),
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported by resident",
                false,
                10L
        );
        trigger.setId(id);
        return trigger;
    }
}

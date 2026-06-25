package com.infratrack.asset;

import com.infratrack.asset.dto.RegisterAssetRequest;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
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
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AssetCategoryRepository assetCategoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AssetService assetService;

    @Test
    void registerAsset_shouldCreateAssetAndHistoryEvent_whenValid() {
        RegisterAssetRequest request = validRequest();
        Department department = department(1L, "Parks");
        AssetCategory category = category(2L, "Playground");
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(assetCategoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(assetRepository.existsByNameIgnoreCaseAndDepartmentIdAndAssetCategoryId(
                "Central Playground", 1L, 2L)).thenReturn(false);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> {
            Asset asset = invocation.getArgument(0);
            asset.setId(100L);
            return asset;
        });

        var response = assetService.registerAsset(request, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Central Playground");
        assertThat(response.getDepartmentName()).isEqualTo("Parks");
        assertThat(response.getAssetCategoryName()).isEqualTo("Playground");
        assertThat(response.getRegistrationDate()).isEqualTo(LocalDate.of(2026, 6, 25));

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.ASSET_REGISTERED);
        assertThat(historyCaptor.getValue().getPerformedByUserId()).isEqualTo(10L);
        assertThat(historyCaptor.getValue().getEventDate()).isEqualTo(LocalDate.of(2026, 6, 25));

        verify(assetRepository, times(1)).save(any(Asset.class));
        verify(assetHistoryEventRepository, times(1)).save(any(AssetHistoryEvent.class));
    }

    @Test
    void registerAsset_shouldRejectBlankName() {
        RegisterAssetRequest request = validRequest();
        request.setName("  ");
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerAsset_shouldRejectMissingRegistrationDate() {
        RegisterAssetRequest request = validRequest();
        request.setRegistrationDate(null);
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerAsset_shouldRejectMissingStatus() {
        RegisterAssetRequest request = validRequest();
        request.setStatus(null);
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerAsset_shouldRejectInvalidDepartment() {
        RegisterAssetRequest request = validRequest();
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerAsset_shouldRejectInvalidAssetCategory() {
        RegisterAssetRequest request = validRequest();
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L, "Parks")));
        when(assetCategoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerAsset_shouldRejectDuplicateAsset() {
        RegisterAssetRequest request = validRequest();
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department(1L, "Parks")));
        when(assetCategoryRepository.findById(2L)).thenReturn(Optional.of(category(2L, "Playground")));
        when(assetRepository.existsByNameIgnoreCaseAndDepartmentIdAndAssetCategoryId(
                "Central Playground", 1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);

        verify(assetRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void registerAsset_shouldRejectContractor() {
        RegisterAssetRequest request = validRequest();
        User contractor = user(10L, UserRole.CONTRACTOR);
        when(userService.getById(10L)).thenReturn(contractor);

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);

        verify(assetRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void registerAsset_shouldRejectUnauthorizedRole() {
        RegisterAssetRequest request = validRequest();
        User fieldEmployee = user(10L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(10L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);

        verify(assetRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void registerAsset_shouldRejectAdministrator() {
        RegisterAssetRequest request = validRequest();
        User administrator = user(10L, UserRole.ADMINISTRATOR);
        when(userService.getById(10L)).thenReturn(administrator);

        assertThatThrownBy(() -> assetService.registerAsset(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);

        verify(assetRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    private RegisterAssetRequest validRequest() {
        RegisterAssetRequest request = new RegisterAssetRequest();
        request.setName("Central Playground");
        request.setDepartmentId(1L);
        request.setAssetCategoryId(2L);
        request.setLocation("Memorial Park");
        request.setStatus(AssetStatus.ACTIVE);
        request.setRegistrationDate(LocalDate.of(2026, 6, 25));
        return request;
    }

    private Department department(Long id, String name) {
        Department department = new Department(name);
        department.setId(id);
        return department;
    }

    private AssetCategory category(Long id, String name) {
        AssetCategory category = new AssetCategory(name);
        category.setId(id);
        return category;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}

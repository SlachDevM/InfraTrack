package com.infratrack.asset;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetQrCodeServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

    @Mock
    private QrCodeGenerator qrCodeGenerator;

    private AssetAuthorizationService assetAuthorizationService;
    private AssetQrCodeService assetQrCodeService;

    @BeforeEach
    void setUp() {
        assetAuthorizationService = new AssetAuthorizationService(delegatedAuthorityService);
        assetQrCodeService = new AssetQrCodeService(
                userService,
                assetRepository,
                assetAuthorizationService,
                qrCodeGenerator);
    }

    @Test
    void generateQrCodePng_unknownAsset_throwsNotFoundException() {
        User admin = user(1L, UserRole.ADMINISTRATOR, null);
        when(userService.getById(1L)).thenReturn(admin);
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetQrCodeService.generateQrCodePng(1L, 999L))
                .isInstanceOf(NotFoundException.class);

        verify(qrCodeGenerator, never()).generatePng(any());
    }

    @Test
    void generateQrCodePng_administrator_returnsPngForAssetCode() {
        User admin = user(1L, UserRole.ADMINISTRATOR, null);
        Asset asset = asset(5L, 1L);
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        when(userService.getById(1L)).thenReturn(admin);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(qrCodeGenerator.generatePng(asset.getCode())).thenReturn(png);

        byte[] result = assetQrCodeService.generateQrCodePng(1L, 5L);

        assertThat(result).isSameAs(png);
        verify(qrCodeGenerator).generatePng(asset.getCode());
    }

    @Test
    void generateQrCodePng_managerOwnDepartment_returnsPng() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 1L);
        Asset asset = asset(5L, 1L);
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G'};
        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);
        when(qrCodeGenerator.generatePng(asset.getCode())).thenReturn(png);

        assertThat(assetQrCodeService.generateQrCodePng(30L, 5L)).isSameAs(png);
    }

    @Test
    void generateQrCodePng_managerDelegatedDepartment_returnsPng() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        Asset asset = asset(5L, 1L);
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G'};
        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);
        when(qrCodeGenerator.generatePng(asset.getCode())).thenReturn(png);

        assertThat(assetQrCodeService.generateQrCodePng(30L, 5L)).isSameAs(png);
    }

    @Test
    void generateQrCodePng_managerCrossDepartment_isForbidden() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        Asset asset = asset(5L, 1L);
        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> assetQrCodeService.generateQrCodePng(30L, 5L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(qrCodeGenerator, never()).generatePng(any());
    }

    @Test
    void generateQrCodePng_operationalCoordinatorOwnDepartment_returnsPng() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 1L);
        Asset asset = asset(5L, 1L);
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G'};
        when(userService.getById(40L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(qrCodeGenerator.generatePng(asset.getCode())).thenReturn(png);

        assertThat(assetQrCodeService.generateQrCodePng(40L, 5L)).isSameAs(png);
    }

    @Test
    void generateQrCodePng_fieldEmployeeOwnDepartment_returnsPng() {
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);
        Asset asset = asset(5L, 1L);
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G'};
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(qrCodeGenerator.generatePng(asset.getCode())).thenReturn(png);

        assertThat(assetQrCodeService.generateQrCodePng(20L, 5L)).isSameAs(png);
    }

    @Test
    void generateQrCodePng_fieldEmployeeCrossDepartment_isForbidden() {
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 2L);
        Asset asset = asset(5L, 1L);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> assetQrCodeService.generateQrCodePng(20L, 5L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(qrCodeGenerator, never()).generatePng(any());
    }

    private Asset asset(Long id, Long departmentId) {
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 1),
                10L);
        asset.setId(id);
        return asset;
    }

    private User user(Long id, UserRole role, Long departmentId) {
        User user = new User("user" + id + "@test.com", "password", "User " + id, role);
        user.setId(id);
        user.setEnabled(true);
        if (departmentId != null) {
            Department department = new Department("Department " + departmentId);
            department.setId(departmentId);
            user.setDepartment(department);
        }
        return user;
    }

    private User userInDepartment(Long id, UserRole role, Long departmentId) {
        return user(id, role, departmentId);
    }
}

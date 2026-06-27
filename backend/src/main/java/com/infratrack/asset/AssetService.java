package com.infratrack.asset;

import com.infratrack.asset.dto.AssetResponse;
import com.infratrack.asset.dto.RegisterAssetRequest;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final UserService userService;

    public AssetService(
            AssetRepository assetRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            DepartmentRepository departmentRepository,
            AssetCategoryRepository assetCategoryRepository,
            UserService userService) {
        this.assetRepository = assetRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.departmentRepository = departmentRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> listAll() {
        return assetRepository.findAllByOrderByNameAsc().stream()
                .map(AssetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse getById(Long id) {
        return AssetResponse.from(findAssetOrThrow(id));
    }

    @Transactional
    public AssetResponse registerAsset(RegisterAssetRequest request, Long userId) {
        requireCanRegisterAssets(userId);

        String name = normalizeName(request.getName());
        String location = normalizeLocation(request.getLocation());
        validateRegistrationDate(request.getRegistrationDate());
        validateStatus(request.getStatus());

        Department department = findDepartmentOrThrow(request.getDepartmentId());
        AssetCategory category = findAssetCategoryOrThrow(request.getAssetCategoryId());

        if (assetRepository.existsByNameIgnoreCaseAndDepartmentIdAndAssetCategoryId(
                name, department.getId(), category.getId())) {
            throw new ConflictException(
                    "A possible duplicate asset already exists with the same name, department and category");
        }

        Asset asset = assetRepository.save(new Asset(
                name,
                department,
                category,
                location,
                request.getStatus(),
                request.getRegistrationDate(),
                userId
        ));

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.ASSET_REGISTERED,
                userId,
                request.getRegistrationDate()
        ));

        return AssetResponse.from(asset);
    }

    public void requireCanRegisterAssets(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager() && !user.getRole().isOperationalCoordinator()) {
            throw new ForbiddenOperationException(
                    "Only managers and operational coordinators can register assets");
        }
    }

    private Asset findAssetOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
    }

    private Department findDepartmentOrThrow(Long departmentId) {
        if (departmentId == null) {
            throw new BusinessValidationException("Department is required");
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessValidationException("Invalid department"));
    }

    private AssetCategory findAssetCategoryOrThrow(Long assetCategoryId) {
        if (assetCategoryId == null) {
            throw new BusinessValidationException("Asset category is required");
        }
        return assetCategoryRepository.findById(assetCategoryId)
                .orElseThrow(() -> new BusinessValidationException("Invalid asset category"));
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessValidationException("Asset name is required");
        }
        return name.trim();
    }

    private String normalizeLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new BusinessValidationException("Asset location is required");
        }
        return location.trim();
    }

    private void validateRegistrationDate(java.time.LocalDate registrationDate) {
        if (registrationDate == null) {
            throw new BusinessValidationException("Registration date is required");
        }
    }

    private void validateStatus(AssetStatus status) {
        if (status == null) {
            throw new BusinessValidationException("Asset status is required");
        }
    }
}

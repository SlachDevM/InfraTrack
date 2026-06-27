package com.infratrack.assetcategory;

import com.infratrack.assetcategory.dto.AssetCategoryResponse;
import com.infratrack.assetcategory.dto.CreateAssetCategoryRequest;
import com.infratrack.assetcategory.dto.UpdateAssetCategoryRequest;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetCategoryService {

    private final AssetCategoryRepository assetCategoryRepository;

    public AssetCategoryService(AssetCategoryRepository assetCategoryRepository) {
        this.assetCategoryRepository = assetCategoryRepository;
    }

    public List<AssetCategoryResponse> listAll() {
        return assetCategoryRepository.findAllByOrderByNameAsc().stream()
                .map(AssetCategoryResponse::from)
                .toList();
    }

    public AssetCategoryResponse getById(Long id) {
        return AssetCategoryResponse.from(findCategoryOrThrow(id));
    }

    public AssetCategoryResponse create(CreateAssetCategoryRequest request) {
        String name = normalizeName(request.getName());
        if (assetCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessValidationException("Asset category name already exists");
        }

        AssetCategory category = assetCategoryRepository.save(new AssetCategory(name));
        return AssetCategoryResponse.from(category);
    }

    public AssetCategoryResponse update(Long id, UpdateAssetCategoryRequest request) {
        AssetCategory category = findCategoryOrThrow(id);
        String name = normalizeName(request.getName());

        if (assetCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessValidationException("Asset category name already exists");
        }

        category.setName(name);
        category.setUpdatedAt(System.currentTimeMillis());
        return AssetCategoryResponse.from(assetCategoryRepository.save(category));
    }

    public void delete(Long id) {
        if (!assetCategoryRepository.existsById(id)) {
            throw new NotFoundException("Asset category not found");
        }
        assetCategoryRepository.deleteById(id);
    }

    private AssetCategory findCategoryOrThrow(Long id) {
        return assetCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset category not found"));
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessValidationException("Asset category name is required");
        }
        return name.trim();
    }
}

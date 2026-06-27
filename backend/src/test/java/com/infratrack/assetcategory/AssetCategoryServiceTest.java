package com.infratrack.assetcategory;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.assetcategory.dto.AssetCategoryResponse;
import com.infratrack.assetcategory.dto.CreateAssetCategoryRequest;
import com.infratrack.assetcategory.dto.UpdateAssetCategoryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetCategoryServiceTest {

    @Mock
    private AssetCategoryRepository assetCategoryRepository;

    @InjectMocks
    private AssetCategoryService assetCategoryService;

    @Test
    void listAll_shouldReturnCategoriesOrderedByName() {
        AssetCategory bridge = new AssetCategory("Bridge");
        bridge.setId(1L);
        AssetCategory playground = new AssetCategory("Playground");
        playground.setId(2L);

        when(assetCategoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(bridge, playground));

        List<AssetCategoryResponse> result = assetCategoryService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Bridge");
        assertThat(result.get(1).getName()).isEqualTo("Playground");
    }

    @Test
    void create_shouldSaveCategory_whenNameIsValid() {
        CreateAssetCategoryRequest request = new CreateAssetCategoryRequest();
        request.setName("Building");

        when(assetCategoryRepository.existsByNameIgnoreCase("Building")).thenReturn(false);
        when(assetCategoryRepository.save(any(AssetCategory.class))).thenAnswer(invocation -> {
            AssetCategory category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });

        AssetCategoryResponse result = assetCategoryService.create(request);

        assertThat(result.getName()).isEqualTo("Building");
        verify(assetCategoryRepository).save(any(AssetCategory.class));
    }

    @Test
    void create_shouldRejectBlankName() {
        CreateAssetCategoryRequest request = new CreateAssetCategoryRequest();
        request.setName("");

        assertThatThrownBy(() -> assetCategoryService.create(request))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void create_shouldRejectDuplicateName() {
        CreateAssetCategoryRequest request = new CreateAssetCategoryRequest();
        request.setName("Road");

        when(assetCategoryRepository.existsByNameIgnoreCase("Road")).thenReturn(true);

        assertThatThrownBy(() -> assetCategoryService.create(request))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(assetCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetCategoryService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldUpdateName_whenValid() {
        AssetCategory category = new AssetCategory("Road");
        category.setId(1L);

        UpdateAssetCategoryRequest request = new UpdateAssetCategoryRequest();
        request.setName("Bridge");

        when(assetCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(assetCategoryRepository.existsByNameIgnoreCaseAndIdNot("Bridge", 1L)).thenReturn(false);
        when(assetCategoryRepository.save(category)).thenReturn(category);

        AssetCategoryResponse result = assetCategoryService.update(1L, request);

        assertThat(result.getName()).isEqualTo("Bridge");
    }

    @Test
    void delete_shouldRemoveCategory_whenExists() {
        when(assetCategoryRepository.existsById(1L)).thenReturn(true);

        assetCategoryService.delete(1L);

        verify(assetCategoryRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(assetCategoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> assetCategoryService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}

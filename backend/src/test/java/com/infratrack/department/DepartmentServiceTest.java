package com.infratrack.department;

import com.infratrack.asset.AssetRepository;
import com.infratrack.department.dto.CreateDepartmentRequest;
import com.infratrack.department.dto.DepartmentResponse;
import com.infratrack.department.dto.UpdateDepartmentRequest;
import com.infratrack.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void listAll_shouldReturnDepartmentsOrderedByName() {
        Department parks = new Department("Parks");
        parks.setId(1L);
        Department roads = new Department("Roads");
        roads.setId(2L);

        when(departmentRepository.findAllByOrderByNameAsc()).thenReturn(List.of(parks, roads));

        List<DepartmentResponse> result = departmentService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Parks");
        assertThat(result.get(1).getName()).isEqualTo("Roads");
    }

    @Test
    void create_shouldSaveDepartment_whenNameIsValid() {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName("Facilities");

        when(departmentRepository.existsByNameIgnoreCase("Facilities")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department department = invocation.getArgument(0);
            department.setId(1L);
            return department;
        });

        DepartmentResponse result = departmentService.create(request);

        assertThat(result.getName()).isEqualTo("Facilities");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void create_shouldRejectBlankName() {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName("   ");

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_shouldRejectDuplicateName() {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName("Parks");

        when(departmentRepository.existsByNameIgnoreCase("Parks")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }

    @Test
    void update_shouldUpdateName_whenValid() {
        Department department = new Department("Parks");
        department.setId(1L);

        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName("Parks and Gardens");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameIgnoreCaseAndIdNot("Parks and Gardens", 1L)).thenReturn(false);
        when(departmentRepository.save(department)).thenReturn(department);

        DepartmentResponse result = departmentService.update(1L, request);

        assertThat(result.getName()).isEqualTo("Parks and Gardens");
    }

    @Test
    void delete_shouldRemoveDepartment_whenNoAssetsOrUsers() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(assetRepository.existsByDepartmentId(1L)).thenReturn(false);
        when(userRepository.existsByDepartmentId(1L)).thenReturn(false);

        departmentService.delete(1L);

        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void delete_shouldReject_whenAssetsExist() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(assetRepository.existsByDepartmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("assets belong to it");

        verify(departmentRepository, never()).deleteById(any());
        verify(userRepository, never()).existsByDepartmentId(any());
    }

    @Test
    void delete_shouldReject_whenUsersExist() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(assetRepository.existsByDepartmentId(1L)).thenReturn(false);
        when(userRepository.existsByDepartmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("users belong to it");

        verify(departmentRepository, never()).deleteById(any());
    }

    @Test
    void update_shouldNotChangeAssetDepartmentReference() {
        Department department = new Department("Parks");
        department.setId(1L);

        UpdateDepartmentRequest request = new UpdateDepartmentRequest();
        request.setName("Parks and Gardens");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameIgnoreCaseAndIdNot("Parks and Gardens", 1L)).thenReturn(false);
        when(departmentRepository.save(department)).thenReturn(department);

        departmentService.update(1L, request);

        assertThat(department.getId()).isEqualTo(1L);
        verify(assetRepository, never()).save(any());
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(departmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> departmentService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
    }
}

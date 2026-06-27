package com.infratrack.department;

import com.infratrack.asset.AssetRepository;
import com.infratrack.department.dto.CreateDepartmentRequest;
import com.infratrack.department.dto.DepartmentResponse;
import com.infratrack.department.dto.UpdateDepartmentRequest;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Council department reference data for user and operational context.
 */
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            AssetRepository assetRepository,
            UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    @Cacheable("departments")
    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse getById(Long id) {
        return DepartmentResponse.from(findDepartmentOrThrow(id));
    }

    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse create(CreateDepartmentRequest request) {
        String name = normalizeName(request.getName());
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessValidationException("Department name already exists");
        }

        Department department = departmentRepository.save(new Department(name));
        return DepartmentResponse.from(department);
    }

    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse update(Long id, UpdateDepartmentRequest request) {
        Department department = findDepartmentOrThrow(id);
        String name = normalizeName(request.getName());

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessValidationException("Department name already exists");
        }

        department.setName(name);
        department.setUpdatedAt(System.currentTimeMillis());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @CacheEvict(value = "departments", allEntries = true)
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new NotFoundException("Department not found");
        }
        if (assetRepository.existsByDepartmentId(id)) {
            throw new BusinessValidationException(
                    "Cannot delete department while assets belong to it");
        }
        if (userRepository.existsByDepartmentId(id)) {
            throw new BusinessValidationException(
                    "Cannot delete department while users belong to it");
        }
        departmentRepository.deleteById(id);
    }

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessValidationException("Department name is required");
        }
        return name.trim();
    }
}

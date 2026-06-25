package com.infratrack.department;

import com.infratrack.department.dto.CreateDepartmentRequest;
import com.infratrack.department.dto.DepartmentResponse;
import com.infratrack.department.dto.UpdateDepartmentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    public DepartmentResponse getById(Long id) {
        return DepartmentResponse.from(findDepartmentOrThrow(id));
    }

    public DepartmentResponse create(CreateDepartmentRequest request) {
        String name = normalizeName(request.getName());
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name already exists");
        }

        Department department = departmentRepository.save(new Department(name));
        return DepartmentResponse.from(department);
    }

    public DepartmentResponse update(Long id, UpdateDepartmentRequest request) {
        Department department = findDepartmentOrThrow(id);
        String name = normalizeName(request.getName());

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name already exists");
        }

        department.setName(name);
        department.setUpdatedAt(System.currentTimeMillis());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
        }
        departmentRepository.deleteById(id);
    }

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name is required");
        }
        return name.trim();
    }
}

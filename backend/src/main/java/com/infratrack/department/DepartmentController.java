package com.infratrack.department;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.department.dto.CreateDepartmentRequest;
import com.infratrack.department.dto.DepartmentResponse;
import com.infratrack.department.dto.UpdateDepartmentRequest;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Departments", description = "Council department reference data")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;

    public DepartmentController(DepartmentService departmentService, UserService userService) {
        this.departmentService = departmentService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List departments")
    @ApiResponse(responseCode = "200", description = "Department list")
    public ResponseEntity<List<DepartmentResponse>> listDepartments() {
        return ResponseEntity.ok(departmentService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID")
    @ApiResponse(responseCode = "200", description = "Department details")
    public ResponseEntity<DepartmentResponse> getDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create department", description = "Administrator only.")
    @ApiResponse(responseCode = "201", description = "Department created")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            Authentication authentication) {
        requireAdministrator(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Administrator only.")
    @ApiResponse(responseCode = "200", description = "Department updated")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request,
            Authentication authentication) {
        requireAdministrator(authentication);
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Administrator only.")
    @ApiResponse(responseCode = "204", description = "Department deleted")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdministrator(authentication);
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdministrator(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isAdministrator(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only administrators can manage departments");
        }
    }
}

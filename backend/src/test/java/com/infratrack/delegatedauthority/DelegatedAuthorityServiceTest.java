package com.infratrack.delegatedauthority;

import com.infratrack.delegatedauthority.dto.CreateDelegatedAuthorityRequest;
import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelegatedAuthorityServiceTest {

    @Mock
    private DelegatedAuthorityRepository delegatedAuthorityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DelegatedAuthorityService delegatedAuthorityService;

    @Test
    void create_shouldCreateValidDelegation() {
        CreateDelegatedAuthorityRequest request = validRequest();
        User delegatingManager = manager(10L, 1L);
        User delegateManager = manager(20L, 2L);
        Department source = department(1L, "Parks");
        Department target = department(2L, "Roads");

        when(userService.getById(10L)).thenReturn(delegatingManager);
        when(userRepository.findById(20L)).thenReturn(Optional.of(delegateManager));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(source));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(target));
        when(delegatedAuthorityRepository.existsActiveDelegation(eq(20L), eq(2L), any(LocalDateTime.class)))
                .thenReturn(false);
        when(delegatedAuthorityRepository.save(any(DelegatedAuthority.class))).thenAnswer(invocation -> {
            DelegatedAuthority authority = invocation.getArgument(0);
            authority.setId(100L);
            return authority;
        });

        var response = delegatedAuthorityService.create(request, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getDelegateManagerUserId()).isEqualTo(20L);
        assertThat(response.getTargetDepartmentId()).isEqualTo(2L);
        verify(delegatedAuthorityRepository).save(any(DelegatedAuthority.class));
    }

    @Test
    void create_shouldRejectNonManager() {
        CreateDelegatedAuthorityRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(10L)).thenReturn(coordinator);

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void create_shouldRejectAdministrator() {
        CreateDelegatedAuthorityRequest request = validRequest();
        when(userService.getById(10L)).thenReturn(user(10L, UserRole.ADMINISTRATOR));

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void create_shouldRejectFieldEmployee() {
        CreateDelegatedAuthorityRequest request = validRequest();
        when(userService.getById(10L)).thenReturn(user(10L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void create_shouldRejectContractor() {
        CreateDelegatedAuthorityRequest request = validRequest();
        when(userService.getById(10L)).thenReturn(user(10L, UserRole.CONTRACTOR));

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void create_shouldRejectNonManagerDelegate() {
        CreateDelegatedAuthorityRequest request = validRequest();
        User delegatingManager = manager(10L, 1L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(10L)).thenReturn(delegatingManager);
        when(userRepository.findById(20L)).thenReturn(Optional.of(fieldEmployee));

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_shouldRejectWhenDelegatingManagerNotInSourceDepartment() {
        CreateDelegatedAuthorityRequest request = validRequest();
        User delegatingManager = manager(10L, 99L);

        when(userService.getById(10L)).thenReturn(delegatingManager);

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void create_shouldRejectBlankReason() {
        CreateDelegatedAuthorityRequest request = validRequest();
        request.setReason("  ");
        when(userService.getById(10L)).thenReturn(manager(10L, 1L));

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_shouldRejectInvalidDateRange() {
        CreateDelegatedAuthorityRequest request = validRequest();
        request.setValidUntil(request.getValidFrom());

        when(userService.getById(10L)).thenReturn(manager(10L, 1L));

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_shouldRejectPastValidUntil() {
        CreateDelegatedAuthorityRequest request = validRequest();
        request.setValidFrom(LocalDateTime.now().minusDays(5));
        request.setValidUntil(LocalDateTime.now().minusDays(1));

        when(userService.getById(10L)).thenReturn(manager(10L, 1L));

        assertThatThrownBy(() -> delegatedAuthorityService.create(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void revoke_shouldAllowDelegatingManager() {
        DelegatedAuthority authority = savedAuthority(100L, 10L);
        when(userService.getById(10L)).thenReturn(manager(10L, 1L));
        when(delegatedAuthorityRepository.findById(100L)).thenReturn(Optional.of(authority));
        when(delegatedAuthorityRepository.save(authority)).thenReturn(authority);

        var response = delegatedAuthorityService.revoke(100L, 10L);

        assertThat(response.isRevoked()).isTrue();
        assertThat(authority.isRevoked()).isTrue();
    }

    @Test
    void revoke_shouldRejectNonDelegatingManager() {
        DelegatedAuthority authority = savedAuthority(100L, 10L);
        when(userService.getById(99L)).thenReturn(manager(99L, 1L));
        when(delegatedAuthorityRepository.findById(100L)).thenReturn(Optional.of(authority));

        assertThatThrownBy(() -> delegatedAuthorityService.revoke(100L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    private CreateDelegatedAuthorityRequest validRequest() {
        CreateDelegatedAuthorityRequest request = new CreateDelegatedAuthorityRequest();
        request.setDelegateManagerUserId(20L);
        request.setSourceDepartmentId(1L);
        request.setTargetDepartmentId(2L);
        request.setReason("Annual leave cover");
        request.setValidFrom(LocalDateTime.now().minusHours(1));
        request.setValidUntil(LocalDateTime.now().plusDays(7));
        return request;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user" + id + "@test.com", "password", "User " + id, role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }

    private User manager(Long id, Long departmentId) {
        User manager = user(id, UserRole.MANAGER);
        manager.setDepartment(department(departmentId, "Dept " + departmentId));
        return manager;
    }

    private Department department(Long id, String name) {
        Department department = new Department(name);
        department.setId(id);
        return department;
    }

    private DelegatedAuthority savedAuthority(Long id, Long delegatingManagerUserId) {
        DelegatedAuthority authority = new DelegatedAuthority(
                delegatingManagerUserId,
                20L,
                department(1L, "Parks"),
                department(2L, "Roads"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(7),
                "Cover");
        authority.setId(id);
        return authority;
    }
}

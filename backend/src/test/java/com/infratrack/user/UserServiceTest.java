package com.infratrack.user;

import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.dto.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        User user = new User("manager@test.com", "password", "Manager", UserRole.MANAGER);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertThat(result).isEqualTo(user);
        assertThat(result.getRole()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    void getById_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void getWorkers_shouldReturnFieldEmployeesAndContractors() {
        User fieldEmployee = new User("field@test.com", "password", "Field Worker", UserRole.FIELD_EMPLOYEE);
        fieldEmployee.setId(1L);

        User contractor = new User("contractor@test.com", "password", "Contractor", UserRole.CONTRACTOR);
        contractor.setId(2L);

        when(userRepository.findByRoleInOrderByNameAsc(
                List.of(UserRole.FIELD_EMPLOYEE, UserRole.CONTRACTOR)
        )).thenReturn(List.of(fieldEmployee, contractor));

        List<UserSummary> result = userService.getWorkers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Field Worker");
        assertThat(result.get(1).getName()).isEqualTo("Contractor");

        verify(userRepository).findByRoleInOrderByNameAsc(
                List.of(UserRole.FIELD_EMPLOYEE, UserRole.CONTRACTOR)
        );
    }

    @Test
    void isAdministrator_shouldReturnTrue_onlyForAdministrator() {
        User administrator = new User("admin@test.com", "password", "Admin", UserRole.ADMINISTRATOR);
        administrator.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(administrator));

        assertThat(userService.isAdministrator(1L)).isTrue();
    }

    @Test
    void isAdministrator_shouldReturnFalse_forNonAdministrators() {
        User manager = new User("manager@test.com", "password", "Manager", UserRole.MANAGER);
        manager.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));

        assertThat(userService.isAdministrator(1L)).isFalse();
    }

    @Test
    void findByName_shouldReturnMatchingUsers() {
        User worker = new User("worker@test.com", "password", "John Worker", UserRole.FIELD_EMPLOYEE);
        worker.setId(1L);

        when(userRepository.findByName("John Worker")).thenReturn(List.of(worker));

        List<User> result = userService.findByName("John Worker");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Worker");
    }

    @Test
    void getEligibleWorkersForAssignment_shouldReturnWorkersForCoordinatorDepartment() {
        Department department = new Department("Parks");
        department.setId(1L);
        User coordinator = new User("coord@test.com", "password", "Coordinator", UserRole.OPERATIONAL_COORDINATOR);
        coordinator.setId(40L);
        coordinator.setDepartment(department);
        User fieldEmployee = new User("field@test.com", "password", "Field Worker", UserRole.FIELD_EMPLOYEE);
        fieldEmployee.setId(20L);
        fieldEmployee.setDepartment(department);
        fieldEmployee.setEnabled(true);

        when(userRepository.findById(40L)).thenReturn(Optional.of(coordinator));
        when(userRepository.findByRoleAndDepartmentIdAndEnabledTrueOrderByNameAsc(
                UserRole.FIELD_EMPLOYEE, 1L)).thenReturn(List.of(fieldEmployee));

        List<UserSummary> result = userService.getEligibleWorkersForAssignment(
                40L, 1L, UserRole.FIELD_EMPLOYEE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Field Worker");
    }

    @Test
    void getEligibleWorkersForAssignment_shouldRejectCrossDepartmentForCoordinator() {
        Department coordinatorDepartment = new Department("Roads");
        coordinatorDepartment.setId(2L);
        User coordinator = new User("coord@test.com", "password", "Coordinator", UserRole.OPERATIONAL_COORDINATOR);
        coordinator.setId(40L);
        coordinator.setDepartment(coordinatorDepartment);

        when(userRepository.findById(40L)).thenReturn(Optional.of(coordinator));

        assertThatThrownBy(() -> userService.getEligibleWorkersForAssignment(
                40L, 1L, UserRole.FIELD_EMPLOYEE))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(userRepository, never()).findByRoleAndDepartmentIdAndEnabledTrueOrderByNameAsc(any(), any());
    }

    @Test
    void updateFcmToken_shouldUpdateTokenAndUpdateTimestamp() {
        User user = new User("worker@test.com", "password", "John Worker", UserRole.FIELD_EMPLOYEE);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateFcmToken(1L, "firebase-token-12345");

        assertThat(result.getFcmToken()).isEqualTo("firebase-token-12345");
        verify(userRepository).save(any(User.class));
    }
}

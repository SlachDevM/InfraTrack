package com.infratrack.user;

import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.dto.UserSummary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

/**
 * Resolves users by identity and role for authentication and operational lookups.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<UserSummary> getWorkers() {
        return userRepository
                .findByRoleInOrderByNameAsc(
                        Arrays.asList(
                                UserRole.FIELD_EMPLOYEE,
                                UserRole.CONTRACTOR
                        )
                )
                .stream()
                .map(UserSummary::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummary> getEligibleWorkersForAssignment(Long userId, Long departmentId, UserRole role) {
        if (departmentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department is required");
        }
        if (role == null || (!role.isFieldEmployee() && !role.isContractor())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be field employee or contractor");
        }
        User requester = getById(userId);
        if (requester.getRole().isOperationalCoordinator()) {
            Department requesterDepartment = requester.getDepartment();
            if (requesterDepartment == null
                    || !requesterDepartment.getId().equals(departmentId)) {
                throw new ForbiddenOperationException(
                        "You may only list workers from your own department.");
            }
        }
        return userRepository.findByRoleAndDepartmentIdAndEnabledTrueOrderByNameAsc(role, departmentId)
                .stream()
                .map(UserSummary::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummary> getManagers() {
        return userRepository.findByRoleOrderByNameAsc(UserRole.MANAGER).stream()
                .map(UserSummary::new)
                .toList();
    }

    public boolean isAdministrator(Long userId) {
        return getById(userId).getRole().isAdministrator();
    }

    public boolean isManager(Long userId) {
        return getById(userId).getRole().isManager();
    }

    public boolean isOperationalCoordinator(Long userId) {
        return getById(userId).getRole().isOperationalCoordinator();
    }

    public boolean isFieldEmployee(Long userId) {
        return getById(userId).getRole().isFieldEmployee();
    }

    public boolean isContractor(Long userId) {
        return getById(userId).getRole().isContractor();
    }

    @Transactional(readOnly = true)
    public List<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    @Transactional
    public User updateFcmToken(Long userId, String fcmToken) {
        User user = getById(userId);
        user.setFcmToken(fcmToken);
        user.setUpdatedAt(System.currentTimeMillis());
        return userRepository.save(user);
    }
}

package com.infratrack.user;

import com.infratrack.user.dto.UserSummary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

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

    public List<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public User updateFcmToken(Long userId, String fcmToken) {
        User user = getById(userId);
        user.setFcmToken(fcmToken);
        user.setUpdatedAt(System.currentTimeMillis());
        return userRepository.save(user);
    }
}

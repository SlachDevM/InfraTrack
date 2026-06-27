package com.infratrack.delegatedauthority;

import com.infratrack.delegatedauthority.dto.CreateDelegatedAuthorityRequest;
import com.infratrack.delegatedauthority.dto.DelegatedAuthorityResponse;
import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Creates and revokes temporary manager authority delegations (UC-008).
 */
@Service
public class DelegatedAuthorityService {

    private final DelegatedAuthorityRepository delegatedAuthorityRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public DelegatedAuthorityService(
            DelegatedAuthorityRepository delegatedAuthorityRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            UserService userService) {
        this.delegatedAuthorityRepository = delegatedAuthorityRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<DelegatedAuthorityResponse> listAll() {
        return delegatedAuthorityRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DelegatedAuthorityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DelegatedAuthorityResponse getById(Long id) {
        return DelegatedAuthorityResponse.from(findAuthorityOrThrow(id));
    }

    @Transactional
    public DelegatedAuthorityResponse create(CreateDelegatedAuthorityRequest request, Long userId) {
        User delegatingManager = requireManager(userId);
        requireManagerBelongsToSourceDepartment(delegatingManager, request.getSourceDepartmentId());

        User delegateManager = findDelegateManagerOrThrow(request.getDelegateManagerUserId());
        Department sourceDepartment = findDepartmentOrThrow(request.getSourceDepartmentId());
        Department targetDepartment = findDepartmentOrThrow(request.getTargetDepartmentId());

        String reason = normalizeReason(request.getReason());
        LocalDateTime validFrom = requireValidFrom(request.getValidFrom());
        LocalDateTime validUntil = requireValidUntil(request.getValidUntil());
        validateValidityPeriod(validFrom, validUntil);

        if (delegatedAuthorityRepository.existsActiveDelegation(
                delegateManager.getId(),
                targetDepartment.getId(),
                LocalDateTime.now())) {
            throw new ConflictException(
                    "An active delegation already exists for this delegate and target department");
        }

        DelegatedAuthority authority = delegatedAuthorityRepository.save(new DelegatedAuthority(
                delegatingManager.getId(),
                delegateManager.getId(),
                sourceDepartment,
                targetDepartment,
                validFrom,
                validUntil,
                reason
        ));

        return DelegatedAuthorityResponse.from(authority);
    }

    @Transactional
    public DelegatedAuthorityResponse revoke(Long id, Long userId) {
        User manager = requireManager(userId);
        DelegatedAuthority authority = findAuthorityOrThrow(id);

        if (!authority.getDelegatingManagerUserId().equals(manager.getId())) {
            throw new ForbiddenOperationException(
                    "Only the delegating manager may revoke this delegation");
        }

        if (authority.isRevoked()) {
            throw new BusinessValidationException("Delegation is already revoked");
        }

        authority.revoke(manager.getId(), LocalDateTime.now());
        return DelegatedAuthorityResponse.from(delegatedAuthorityRepository.save(authority));
    }

    @Transactional(readOnly = true)
    public Optional<DelegatedAuthority> findActiveDelegation(
            Long delegateManagerUserId,
            Long targetDepartmentId,
            LocalDateTime at) {
        return delegatedAuthorityRepository.findActiveDelegation(
                delegateManagerUserId,
                targetDepartmentId,
                at);
    }

    public boolean isManagerOfDepartment(User manager, Department department) {
        return manager.getRole().isManager()
                && manager.getDepartment() != null
                && department != null
                && manager.getDepartment().getId().equals(department.getId());
    }

    User requireManager(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager()) {
            throw new ForbiddenOperationException(
                    "Only managers can manage delegated authority");
        }
        return user;
    }

    private void requireManagerBelongsToSourceDepartment(User manager, Long sourceDepartmentId) {
        if (sourceDepartmentId == null) {
            throw new BusinessValidationException("Source department is required");
        }
        if (manager.getDepartment() == null || !manager.getDepartment().getId().equals(sourceDepartmentId)) {
            throw new ForbiddenOperationException(
                    "Delegating manager must belong to the source department");
        }
    }

    private User findDelegateManagerOrThrow(Long delegateManagerUserId) {
        if (delegateManagerUserId == null) {
            throw new BusinessValidationException("Delegate manager is required");
        }
        User delegate = userRepository.findById(delegateManagerUserId)
                .orElseThrow(() -> new BusinessValidationException("Delegate manager not found"));
        if (!delegate.getRole().isManager()) {
            throw new BusinessValidationException("Delegate must be a manager");
        }
        return delegate;
    }

    private Department findDepartmentOrThrow(Long departmentId) {
        if (departmentId == null) {
            throw new BusinessValidationException("Department is required");
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessValidationException("Department not found"));
    }

    private DelegatedAuthority findAuthorityOrThrow(Long id) {
        return delegatedAuthorityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Delegated authority not found"));
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessValidationException("Delegation reason is required");
        }
        return reason.trim();
    }

    private LocalDateTime requireValidFrom(LocalDateTime validFrom) {
        if (validFrom == null) {
            throw new BusinessValidationException("Valid from date and time is required");
        }
        return validFrom;
    }

    private LocalDateTime requireValidUntil(LocalDateTime validUntil) {
        if (validUntil == null) {
            throw new BusinessValidationException("Valid until date and time is required");
        }
        return validUntil;
    }

    private void validateValidityPeriod(LocalDateTime validFrom, LocalDateTime validUntil) {
        if (!validUntil.isAfter(validFrom)) {
            throw new BusinessValidationException(
                    "Valid until must be after valid from");
        }
        if (!validUntil.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException(
                    "Valid until must be in the future");
        }
    }
}

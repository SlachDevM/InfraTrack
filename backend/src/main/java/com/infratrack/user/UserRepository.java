package com.infratrack.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"department"})
    Optional<User> findById(Long id);

    /**
     * Loads all users with departments in a single query to avoid N+1 when mapping
     * {@code UserManagementResponse} department fields.
     */
    @Override
    @EntityGraph(attributePaths = {"department"})
    List<User> findAll();

    List<User> findByRoleInOrderByNameAsc(List<UserRole> roles);

    List<User> findByName(String name);

    List<User> findByRoleAndDepartmentId(UserRole role, Long departmentId);

    @EntityGraph(attributePaths = {"department"})
    List<User> findByRoleAndDepartmentIdAndEnabledTrueOrderByNameAsc(UserRole role, Long departmentId);

    List<User> findByRoleOrderByNameAsc(UserRole role);

    boolean existsByDepartmentId(Long departmentId);

    boolean existsByRole(UserRole role);

    boolean existsByIdAndEnabledTrue(Long id);
}

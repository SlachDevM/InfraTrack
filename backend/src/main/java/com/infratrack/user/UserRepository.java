package com.infratrack.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRoleInOrderByNameAsc(List<UserRole> roles);

    List<User> findByName(String name);

    List<User> findByRoleAndDepartmentId(UserRole role, Long departmentId);

    List<User> findByRoleOrderByNameAsc(UserRole role);

    boolean existsByDepartmentId(Long departmentId);
}

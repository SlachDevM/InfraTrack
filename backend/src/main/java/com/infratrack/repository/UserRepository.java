package com.infratrack.repository;

import com.infratrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infratrack.model.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRoleInOrderByNameAsc(List<UserRole> roles);

    List<User> findByName(String name);
}

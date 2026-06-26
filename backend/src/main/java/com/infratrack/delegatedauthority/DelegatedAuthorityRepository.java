package com.infratrack.delegatedauthority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DelegatedAuthorityRepository extends JpaRepository<DelegatedAuthority, Long> {

    List<DelegatedAuthority> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT d FROM DelegatedAuthority d
            WHERE d.delegateManagerUserId = :delegateManagerUserId
              AND d.targetDepartment.id = :targetDepartmentId
              AND d.revoked = false
              AND d.validFrom <= :at
              AND d.validUntil > :at
            """)
    Optional<DelegatedAuthority> findActiveDelegation(
            @Param("delegateManagerUserId") Long delegateManagerUserId,
            @Param("targetDepartmentId") Long targetDepartmentId,
            @Param("at") LocalDateTime at);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DelegatedAuthority d
            WHERE d.delegateManagerUserId = :delegateManagerUserId
              AND d.targetDepartment.id = :targetDepartmentId
              AND d.revoked = false
              AND d.validFrom <= :at
              AND d.validUntil > :at
            """)
    boolean existsActiveDelegation(
            @Param("delegateManagerUserId") Long delegateManagerUserId,
            @Param("targetDepartmentId") Long targetDepartmentId,
            @Param("at") LocalDateTime at);
}

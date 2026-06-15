package com.mrrg.backend.controller;

import com.mrrg.backend.dto.UserSummary;
import com.mrrg.backend.model.User;
import com.mrrg.backend.model.UserRole;
import com.mrrg.backend.repository.UserRepository;
import com.mrrg.backend.security.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/workers")
    public ResponseEntity<List<UserSummary>> getWorkers(Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        User user = userRepository.findById(token.getUserId()).orElse(null);
        if (user == null || (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UserSummary> workers = userRepository
                .findByRoleInOrderByNameAsc(Arrays.asList(UserRole.EMPLOYEE, UserRole.MANAGER))
                .stream()
                .map(UserSummary::new)
                .toList();

        return ResponseEntity.ok(workers);
    }
}

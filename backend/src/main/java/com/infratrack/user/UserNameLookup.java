package com.infratrack.user;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserNameLookup {

    private final UserRepository userRepository;

    public UserNameLookup(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<Long, String> resolveNames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(
                userIds.stream().filter(Objects::nonNull).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(User::getId, User::getName));
    }
}

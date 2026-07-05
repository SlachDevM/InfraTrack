package com.infratrack.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.infratrack.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserAccountStatusService {

    static final int ACCOUNT_STATUS_CACHE_TTL_SECONDS = 30;
    static final int ACCOUNT_STATUS_CACHE_MAX_SIZE = 10_000;

    private final UserRepository userRepository;
    private final Cache<Long, Boolean> enabledCache;

    public UserAccountStatusService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.enabledCache = Caffeine.newBuilder()
                .expireAfterWrite(ACCOUNT_STATUS_CACHE_TTL_SECONDS, TimeUnit.SECONDS)
                .maximumSize(ACCOUNT_STATUS_CACHE_MAX_SIZE)
                .build();
    }

    public boolean isEnabled(Long userId) {
        if (userId == null) {
            return false;
        }
        return Boolean.TRUE.equals(enabledCache.get(userId, this::loadEnabledFromDatabase));
    }

    public void evict(Long userId) {
        if (userId != null) {
            enabledCache.invalidate(userId);
        }
    }

    private Boolean loadEnabledFromDatabase(Long userId) {
        return userRepository.existsByIdAndEnabledTrue(userId);
    }
}

package com.infratrack.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.infratrack.user.EmailNormalizer;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LoginRateLimiter {

    public static final String RATE_LIMIT_MESSAGE = "Too many login attempts. Please try again later.";
    public static final String ACTIVATION_RATE_LIMIT_MESSAGE =
            "Too many activation attempts. Please try again later.";

    private final int maxAttemptsPerMinute;
    private final int activationMaxAttemptsPerMinute;
    private final Cache<String, Bucket> buckets;

    public LoginRateLimiter(
            @Value("${app.auth.login-rate-limit.max-attempts-per-minute:10}") int maxAttemptsPerMinute,
            @Value("${app.auth.activate-account-rate-limit.max-attempts-per-minute:10}") int activationMaxAttemptsPerMinute
    ) {
        this.maxAttemptsPerMinute = maxAttemptsPerMinute;
        this.activationMaxAttemptsPerMinute = activationMaxAttemptsPerMinute;
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(2))
                .maximumSize(10_000)
                .build();
    }

    public void checkAllowed(String clientIp, String email) {
        String normalizedEmail = email != null ? EmailNormalizer.normalize(email) : "";
        consumeOrThrow("login:ip:" + clientIp, maxAttemptsPerMinute, RATE_LIMIT_MESSAGE);
        consumeOrThrow("login:email:" + normalizedEmail, maxAttemptsPerMinute, RATE_LIMIT_MESSAGE);
    }

    public void checkActivationAllowed(String clientIp) {
        consumeOrThrow("activate:ip:" + clientIp, activationMaxAttemptsPerMinute, ACTIVATION_RATE_LIMIT_MESSAGE);
    }

    private void consumeOrThrow(String key, int maxAttemptsPerMinute, String message) {
        Bucket bucket = buckets.get(key, ignored -> createBucket(maxAttemptsPerMinute));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            long retryAfterSeconds = Math.max(
                    1L,
                    Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds()
            );
            throw new LoginRateLimitExceededException(message, retryAfterSeconds);
        }
    }

    private Bucket createBucket(int maxAttemptsPerMinute) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(maxAttemptsPerMinute)
                .refillGreedy(maxAttemptsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}

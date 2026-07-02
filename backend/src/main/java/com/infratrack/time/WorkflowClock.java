package com.infratrack.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Authoritative source for workflow audit timestamps.
 */
@Component
public class WorkflowClock {

    private final Clock clock;

    public WorkflowClock(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public long nowMillis() {
        return clock.millis();
    }
}

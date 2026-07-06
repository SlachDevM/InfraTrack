package com.infratrack.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Endpoint timers for high-priority mobile and reporting APIs (V2.5-STAB-3).
 */
@Component
public class MobileEndpointMetricsRecorder {

    private final Timer syncTimer;
    private final Timer syncConflictResolveTimer;
    private final Timer dashboardTimer;
    private final Timer myInspectionsTimer;
    private final Timer assetLookupTimer;

    public MobileEndpointMetricsRecorder(MeterRegistry meterRegistry) {
        this.syncTimer = endpointTimer(meterRegistry, "mobile.endpoint.sync", "POST /api/mobile/sync");
        this.syncConflictResolveTimer = endpointTimer(
                meterRegistry,
                "mobile.endpoint.sync.conflicts.resolve",
                "POST /api/mobile/sync/conflicts/resolve");
        this.dashboardTimer = endpointTimer(meterRegistry, "mobile.endpoint.dashboard", "GET /api/mobile/dashboard");
        this.myInspectionsTimer = endpointTimer(
                meterRegistry,
                "mobile.endpoint.my_inspections",
                "GET /api/mobile/my-inspections");
        this.assetLookupTimer = endpointTimer(
                meterRegistry,
                "mobile.endpoint.assets.lookup",
                "GET /api/mobile/assets/lookup");
    }

    public <T> T recordSync(Supplier<T> action) {
        return record(syncTimer, action);
    }

    public <T> T recordSyncConflictResolve(Supplier<T> action) {
        return record(syncConflictResolveTimer, action);
    }

    public <T> T recordDashboard(Supplier<T> action) {
        return record(dashboardTimer, action);
    }

    public <T> T recordMyInspections(Supplier<T> action) {
        return record(myInspectionsTimer, action);
    }

    public <T> T recordAssetLookup(Supplier<T> action) {
        return record(assetLookupTimer, action);
    }

    private static Timer endpointTimer(MeterRegistry meterRegistry, String name, String description) {
        return Timer.builder(name)
                .description(description)
                .register(meterRegistry);
    }

    private static <T> T record(Timer timer, Supplier<T> action) {
        long start = System.nanoTime();
        try {
            return action.get();
        } finally {
            timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }
}

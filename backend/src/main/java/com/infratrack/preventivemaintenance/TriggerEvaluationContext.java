package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * In-memory context for trigger evaluation (V2 Phase B Sprint B1.3).
 */
public class TriggerEvaluationContext {

    private final PreventiveMaintenancePlan plan;
    private final Asset asset;
    private final PlanBusinessTrigger businessTrigger;
    private final LocalDateTime currentDateTime;
    private final Map<String, Number> meterValues;
    private final Set<String> businessEvents;

    public TriggerEvaluationContext(
            PreventiveMaintenancePlan plan,
            LocalDateTime currentDateTime) {
        this(plan, currentDateTime, Collections.emptyMap(), Collections.emptySet());
    }

    public TriggerEvaluationContext(
            PreventiveMaintenancePlan plan,
            LocalDateTime currentDateTime,
            Map<String, Number> meterValues,
            Set<String> businessEvents) {
        this.plan = plan;
        this.asset = plan.getAsset();
        this.businessTrigger = plan.getBusinessTrigger();
        this.currentDateTime = currentDateTime;
        this.meterValues = meterValues == null ? Collections.emptyMap() : Map.copyOf(meterValues);
        this.businessEvents = businessEvents == null ? Collections.emptySet() : Set.copyOf(businessEvents);
    }

    public PreventiveMaintenancePlan getPlan() {
        return plan;
    }

    public Asset getAsset() {
        return asset;
    }

    public PlanBusinessTrigger getBusinessTrigger() {
        return businessTrigger;
    }

    public LocalDateTime getCurrentDateTime() {
        return currentDateTime;
    }

    public Optional<Number> getMeterValue(String meter) {
        return Optional.ofNullable(meterValues.get(meter));
    }

    public boolean hasBusinessEvent(String event) {
        return businessEvents.contains(event);
    }
}

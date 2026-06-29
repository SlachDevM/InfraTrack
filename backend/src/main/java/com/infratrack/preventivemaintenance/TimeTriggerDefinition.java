package com.infratrack.preventivemaintenance;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

final class TimeTriggerDefinition implements TriggerDefinition {

    private final int every;
    private final PlanTimeUnit unit;

    TimeTriggerDefinition(int every, PlanTimeUnit unit) {
        this.every = every;
        this.unit = unit;
    }

    static TimeTriggerDefinition fromJson(JsonNode root) {
        return new TimeTriggerDefinition(
                root.get("every").asInt(),
                PlanTimeUnit.valueOf(root.get("unit").asText()));
    }

    @Override
    public PlanTriggerType getTriggerType() {
        return PlanTriggerType.TIME;
    }

    @Override
    public TriggerSummary buildSummary() {
        String title = every == 1
                ? "Every " + singularTimeUnit(unit)
                : "Every " + every + " " + pluralTimeUnit(unit);
        String description = "Eligible once every "
                + intervalLabel()
                + " from plan creation.";
        return new TriggerSummary(title, description, PlanTriggerType.TIME);
    }

    @Override
    public TriggerEvaluationOutcome evaluate(TriggerEvaluationContext context) {
        LocalDate referenceDate = toLocalDate(context.getPlan().getCreatedAt());
        LocalDate currentDate = context.getCurrentDateTime().toLocalDate();
        long elapsed = elapsedUnits(referenceDate, currentDate);
        if (elapsed >= every) {
            return new TriggerEvaluationOutcome(true, eligibleReason(), null);
        }
        LocalDate nextEligibleDate = advanceByInterval(referenceDate);
        return new TriggerEvaluationOutcome(
                false,
                "Next execution interval has not been reached.",
                toEpochMillis(nextEligibleDate));
    }

    private LocalDate advanceByInterval(LocalDate referenceDate) {
        return switch (unit) {
            case DAY -> referenceDate.plusDays(every);
            case WEEK -> referenceDate.plusWeeks(every);
            case MONTH -> referenceDate.plusMonths(every);
            case YEAR -> referenceDate.plusYears(every);
        };
    }

    private static Long toEpochMillis(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private long elapsedUnits(LocalDate referenceDate, LocalDate currentDate) {
        return switch (unit) {
            case DAY -> ChronoUnit.DAYS.between(referenceDate, currentDate);
            case WEEK -> ChronoUnit.WEEKS.between(referenceDate, currentDate);
            case MONTH -> ChronoUnit.MONTHS.between(referenceDate, currentDate);
            case YEAR -> ChronoUnit.YEARS.between(referenceDate, currentDate);
        };
    }

    private String eligibleReason() {
        if (every == 1) {
            return switch (unit) {
                case DAY -> "One full day has elapsed.";
                case WEEK -> "One full week has elapsed.";
                case MONTH -> "One full month has elapsed.";
                case YEAR -> "One full year has elapsed.";
            };
        }
        return intervalLabel().substring(0, 1).toUpperCase()
                + intervalLabel().substring(1)
                + " have elapsed.";
    }

    private String intervalLabel() {
        if (every == 1) {
            return "full " + singularTimeUnit(unit);
        }
        return every + " full " + pluralTimeUnit(unit);
    }

    private static LocalDate toLocalDate(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
                .toLocalDate();
    }

    private static String singularTimeUnit(PlanTimeUnit unit) {
        return switch (unit) {
            case DAY -> "day";
            case WEEK -> "week";
            case MONTH -> "month";
            case YEAR -> "year";
        };
    }

    private static String pluralTimeUnit(PlanTimeUnit unit) {
        return switch (unit) {
            case DAY -> "days";
            case WEEK -> "weeks";
            case MONTH -> "months";
            case YEAR -> "years";
        };
    }
}

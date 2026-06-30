package com.infratrack.operationsintelligence;

import com.infratrack.operationsintelligence.dto.TrendDataPointResponse;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups event timestamps into DAY, WEEK, or MONTH buckets and zero-fills missing periods.
 */
final class TrendBucketing {

    private TrendBucketing() {
    }

    static List<String> generatePeriodKeys(LocalDate fromDate, LocalDate toDate, TrendBucket bucket) {
        List<String> periods = new ArrayList<>();
        LocalDate cursor = alignToBucketStart(fromDate, bucket);
        LocalDate end = alignToBucketStart(toDate, bucket);

        while (!cursor.isAfter(end)) {
            periods.add(periodKey(cursor, bucket));
            cursor = advanceBucket(cursor, bucket);
        }
        return periods;
    }

    static List<TrendDataPointResponse> bucketLocalDateTimes(
            List<LocalDateTime> timestamps,
            List<String> periodKeys,
            TrendBucket bucket,
            ZoneId zone) {
        Map<String, Long> counts = initializeCounts(periodKeys);
        for (LocalDateTime timestamp : timestamps) {
            if (timestamp == null) {
                continue;
            }
            String key = periodKey(timestamp.atZone(zone).toLocalDate(), bucket);
            counts.merge(key, 1L, Long::sum);
        }
        return toDataPoints(periodKeys, counts);
    }

    static List<TrendDataPointResponse> bucketEpochMillis(
            List<Long> timestamps,
            List<String> periodKeys,
            TrendBucket bucket,
            ZoneId zone) {
        Map<String, Long> counts = initializeCounts(periodKeys);
        for (Long timestamp : timestamps) {
            if (timestamp == null) {
                continue;
            }
            String key = periodKey(
                    Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate(),
                    bucket);
            counts.merge(key, 1L, Long::sum);
        }
        return toDataPoints(periodKeys, counts);
    }

    private static Map<String, Long> initializeCounts(List<String> periodKeys) {
        Map<String, Long> counts = new HashMap<>();
        for (String period : periodKeys) {
            counts.put(period, 0L);
        }
        return counts;
    }

    private static List<TrendDataPointResponse> toDataPoints(List<String> periodKeys, Map<String, Long> counts) {
        List<TrendDataPointResponse> dataPoints = new ArrayList<>(periodKeys.size());
        for (String period : periodKeys) {
            dataPoints.add(new TrendDataPointResponse(period, counts.getOrDefault(period, 0L)));
        }
        return dataPoints;
    }

    private static String periodKey(LocalDate date, TrendBucket bucket) {
        return switch (bucket) {
            case DAY -> date.toString();
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
            case MONTH -> date.withDayOfMonth(1).toString();
        };
    }

    private static LocalDate alignToBucketStart(LocalDate date, TrendBucket bucket) {
        return switch (bucket) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    private static LocalDate advanceBucket(LocalDate cursor, TrendBucket bucket) {
        return switch (bucket) {
            case DAY -> cursor.plusDays(1);
            case WEEK -> cursor.plusWeeks(1);
            case MONTH -> cursor.plusMonths(1);
        };
    }

    static long daysBetweenInclusive(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    }
}

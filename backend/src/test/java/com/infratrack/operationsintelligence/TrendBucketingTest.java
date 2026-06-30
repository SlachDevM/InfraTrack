package com.infratrack.operationsintelligence;

import com.infratrack.operationsintelligence.dto.TrendDataPointResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrendBucketingTest {

    private static final ZoneId ZONE = ZoneId.of("Australia/Sydney");

    @Test
    void generatePeriodKeys_shouldProduceDailyKeysInOrder() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 3);

        List<String> keys = TrendBucketing.generatePeriodKeys(from, to, TrendBucket.DAY);

        assertThat(keys).containsExactly("2026-06-01", "2026-06-02", "2026-06-03");
    }

    @Test
    void bucketLocalDateTimes_shouldGroupByDayAndFillMissingZeros() {
        List<String> periodKeys = List.of("2026-06-01", "2026-06-02", "2026-06-03");
        List<LocalDateTime> timestamps = List.of(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 14, 0),
                LocalDateTime.of(2026, 6, 3, 9, 0));

        List<TrendDataPointResponse> points = TrendBucketing.bucketLocalDateTimes(
                timestamps, periodKeys, TrendBucket.DAY, ZONE);

        assertThat(points).extracting(TrendDataPointResponse::getPeriod)
                .containsExactly("2026-06-01", "2026-06-02", "2026-06-03");
        assertThat(points).extracting(TrendDataPointResponse::getCount)
                .containsExactly(2L, 0L, 1L);
    }

    @Test
    void bucketEpochMillis_shouldGroupByWeekStart() {
        List<String> periodKeys = List.of("2026-06-01", "2026-06-08");
        long firstWeek = LocalDate.of(2026, 6, 3).atStartOfDay(ZONE).toInstant().toEpochMilli();
        long secondWeek = LocalDate.of(2026, 6, 10).atStartOfDay(ZONE).toInstant().toEpochMilli();

        List<TrendDataPointResponse> points = TrendBucketing.bucketEpochMillis(
                List.of(firstWeek, secondWeek),
                periodKeys,
                TrendBucket.WEEK,
                ZONE);

        assertThat(points).extracting(TrendDataPointResponse::getCount)
                .containsExactly(1L, 1L);
    }
}

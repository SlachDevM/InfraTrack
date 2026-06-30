package com.infratrack.operationsintelligence;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.operationsintelligence.dto.OperationsTrendResponse;
import com.infratrack.operationsintelligence.dto.TrendScopeResponse;
import com.infratrack.operationsintelligence.dto.TrendSeriesResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class OperationsTrendService {

    static final int MAX_RANGE_DAYS = 365;
    static final int DEFAULT_RANGE_DAYS = 30;

    private final OperationsTrendAuthorizationService authorizationService;
    private final OperationsTrendAggregationRepository aggregationRepository;
    private final ZoneId zoneId;

    public OperationsTrendService(
            OperationsTrendAuthorizationService authorizationService,
            OperationsTrendAggregationRepository aggregationRepository) {
        this(authorizationService, aggregationRepository, ZoneId.systemDefault());
    }

    OperationsTrendService(
            OperationsTrendAuthorizationService authorizationService,
            OperationsTrendAggregationRepository aggregationRepository,
            ZoneId zoneId) {
        this.authorizationService = authorizationService;
        this.aggregationRepository = aggregationRepository;
        this.zoneId = zoneId;
    }

    @Transactional(readOnly = true)
    public OperationsTrendResponse getTrends(Long userId, Long fromParam, Long toParam, String bucketParam) {
        OperationsKpiScope scope = authorizationService.resolveScope(userId);
        TrendScopeResponse scopeResponse = toScopeResponse(scope);
        TrendBucket bucket = TrendBucket.parse(bucketParam);

        long to = resolveTo(toParam);
        long from = resolveFrom(fromParam, to);
        validateRange(from, to);

        LocalDate fromDate = Instant.ofEpochMilli(from).atZone(zoneId).toLocalDate();
        LocalDate toDateInclusive = resolveInclusiveEndDate(to);
        var periodKeys = TrendBucketing.generatePeriodKeys(fromDate, toDateInclusive, bucket);

        TrendSeriesResponse series = new TrendSeriesResponse();
        series.setInspectionsCompleted(TrendBucketing.bucketLocalDateTimes(
                aggregationRepository.findCompletedInspectionTimestamps(scope, from, to),
                periodKeys,
                bucket,
                zoneId));
        series.setIssuesCreated(TrendBucketing.bucketLocalDateTimes(
                aggregationRepository.findIssueCreatedTimestamps(scope, from, to),
                periodKeys,
                bucket,
                zoneId));
        series.setWorkOrdersCompleted(TrendBucketing.bucketEpochMillis(
                aggregationRepository.findCompletedWorkOrderTimestamps(scope, from, to),
                periodKeys,
                bucket,
                zoneId));
        series.setPreventiveCandidatesGenerated(TrendBucketing.bucketEpochMillis(
                aggregationRepository.findPreventiveCandidateGeneratedTimestamps(scope, from, to),
                periodKeys,
                bucket,
                zoneId));
        series.setSuggestedActionsAccepted(TrendBucketing.bucketEpochMillis(
                aggregationRepository.findAcceptedSuggestedActionTimestamps(scope, from, to),
                periodKeys,
                bucket,
                zoneId));

        OperationsTrendResponse response = new OperationsTrendResponse();
        response.setFrom(from);
        response.setTo(to);
        response.setBucket(bucket.name());
        response.setScope(scopeResponse);
        response.setSeries(series);
        return response;
    }

    private static TrendScopeResponse toScopeResponse(OperationsKpiScope scope) {
        if (scope.isGlobal()) {
            return TrendScopeResponse.global();
        }
        return TrendScopeResponse.forDepartment(scope.departmentId());
    }

    private long resolveTo(Long toParam) {
        if (toParam == null) {
            return System.currentTimeMillis();
        }
        if (toParam < 0) {
            throw new BusinessValidationException("Trend end time must be a positive epoch millis value.");
        }
        return toParam;
    }

    private long resolveFrom(Long fromParam, long to) {
        if (fromParam == null) {
            return Instant.ofEpochMilli(to)
                    .atZone(zoneId)
                    .toLocalDate()
                    .minusDays(DEFAULT_RANGE_DAYS)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli();
        }
        if (fromParam < 0) {
            throw new BusinessValidationException("Trend start time must be a positive epoch millis value.");
        }
        return fromParam;
    }

    private void validateRange(long from, long to) {
        if (from >= to) {
            throw new BusinessValidationException("Trend start time must be before end time.");
        }
        LocalDate fromDate = Instant.ofEpochMilli(from).atZone(zoneId).toLocalDate();
        LocalDate toDateInclusive = resolveInclusiveEndDate(to);
        if (TrendBucketing.daysBetweenInclusive(fromDate, toDateInclusive) > MAX_RANGE_DAYS) {
            throw new BusinessValidationException("Trend date range cannot exceed " + MAX_RANGE_DAYS + " days.");
        }
    }

    private LocalDate resolveInclusiveEndDate(long to) {
        LocalDate toDate = Instant.ofEpochMilli(to).atZone(zoneId).toLocalDate();
        long startOfToDate = toDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
        if (to == startOfToDate) {
            return toDate.minusDays(1);
        }
        return toDate;
    }
}

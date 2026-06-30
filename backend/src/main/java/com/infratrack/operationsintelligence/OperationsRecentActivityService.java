package com.infratrack.operationsintelligence;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.operationsintelligence.dto.RecentActivityItemResponse;
import com.infratrack.operationsintelligence.dto.RecentActivityResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class OperationsRecentActivityService {

    static final int DEFAULT_LIMIT = 20;
    static final int MIN_LIMIT = 1;
    static final int MAX_LIMIT = 100;

    private final OperationsRecentActivityAuthorizationService authorizationService;
    private final OperationsRecentActivityAggregationRepository aggregationRepository;

    public OperationsRecentActivityService(
            OperationsRecentActivityAuthorizationService authorizationService,
            OperationsRecentActivityAggregationRepository aggregationRepository) {
        this.authorizationService = authorizationService;
        this.aggregationRepository = aggregationRepository;
    }

    @Transactional(readOnly = true)
    public RecentActivityResponse getRecentActivity(Long userId, Integer limitParam) {
        OperationsKpiScope scope = authorizationService.resolveScope(userId);
        int limit = resolveLimit(limitParam);

        List<RecentActivityItemResponse> items = aggregationRepository.findRecentActivityRows(scope, limit).stream()
                .sorted(Comparator.comparing(RecentActivitySourceRow::occurredAt).reversed())
                .limit(limit)
                .map(this::toItemResponse)
                .toList();

        RecentActivityResponse response = new RecentActivityResponse();
        response.setItems(items);
        return response;
    }

    private RecentActivityItemResponse toItemResponse(RecentActivitySourceRow row) {
        RecentActivityItemResponse item = new RecentActivityItemResponse();
        item.setType(row.type().name());
        item.setTitle(row.type().getTitle());
        item.setDescription(row.description());
        item.setAssetId(row.assetId());
        item.setAssetName(row.assetName());
        item.setOccurredAt(row.occurredAt());
        item.setRoute(row.type().getRoute());
        return item;
    }

    private int resolveLimit(Integer limitParam) {
        if (limitParam == null) {
            return DEFAULT_LIMIT;
        }
        if (limitParam < MIN_LIMIT || limitParam > MAX_LIMIT) {
            throw new BusinessValidationException(
                    "Activity limit must be between " + MIN_LIMIT + " and " + MAX_LIMIT + ".");
        }
        return limitParam;
    }
}

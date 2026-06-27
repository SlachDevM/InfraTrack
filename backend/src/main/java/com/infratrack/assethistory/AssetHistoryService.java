package com.infratrack.assethistory;

import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssetHistoryService {

    private final AssetRepository assetRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserRepository userRepository;

    public AssetHistoryService(
            AssetRepository assetRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getAssetHistory(Long assetId) {
        requireAssetExists(assetId);

        List<AssetHistoryEvent> events = assetHistoryEventRepository
                .findByAssetIdOrderByEventDateDescCreatedAtDesc(assetId);
        return mapEvents(events);
    }

    @Transactional(readOnly = true)
    public Page<AssetHistoryResponse> getAssetHistory(Long assetId, Pageable pageable) {
        requireAssetExists(assetId);

        Page<AssetHistoryEvent> events = assetHistoryEventRepository
                .findByAssetIdOrderByEventDateDescCreatedAtDesc(assetId, pageable);
        Map<Long, String> userNamesById = loadUserNames(events.getContent());
        return events.map(event -> toResponse(event, userNamesById.get(event.getPerformedByUserId())));
    }

    private List<AssetHistoryResponse> mapEvents(List<AssetHistoryEvent> events) {
        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, String> userNamesById = loadUserNames(events);

        return events.stream()
                .map(event -> toResponse(event, userNamesById.get(event.getPerformedByUserId())))
                .toList();
    }

    private Map<Long, String> loadUserNames(List<AssetHistoryEvent> events) {
        return userRepository.findAllById(
                events.stream()
                        .map(AssetHistoryEvent::getPerformedByUserId)
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(User::getId, User::getName));
    }

    private AssetHistoryResponse toResponse(AssetHistoryEvent event, String responsibleUserName) {
        return new AssetHistoryResponse(
                event.getEventDate(),
                event.getEventType(),
                event.getPerformedByUserId(),
                responsibleUserName
        );
    }

    private void requireAssetExists(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found");
        }
    }
}

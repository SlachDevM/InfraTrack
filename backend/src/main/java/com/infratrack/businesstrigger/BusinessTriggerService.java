package com.infratrack.businesstrigger;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetRepository;
import com.infratrack.businesstrigger.dto.BusinessTriggerResponse;
import com.infratrack.businesstrigger.dto.CreateBusinessTriggerRequest;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class BusinessTriggerService {

    private final BusinessTriggerRepository businessTriggerRepository;
    private final AssetRepository assetRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;

    public BusinessTriggerService(
            BusinessTriggerRepository businessTriggerRepository,
            AssetRepository assetRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService) {
        this.businessTriggerRepository = businessTriggerRepository;
        this.assetRepository = assetRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<BusinessTriggerResponse> listAll() {
        return businessTriggerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BusinessTriggerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessTriggerResponse getById(Long id) {
        return BusinessTriggerResponse.from(findTriggerOrThrow(id));
    }

    @Transactional
    public BusinessTriggerResponse createBusinessTrigger(CreateBusinessTriggerRequest request, Long userId) {
        requireCanCreateBusinessTriggers(userId);

        Asset asset = findAssetOrThrow(request.getAssetId());
        BusinessTriggerType type = validateType(request.getType());
        String reason = normalizeReason(request.getReason());
        boolean urgent = resolveUrgent(type, request.getUrgent());

        BusinessTrigger trigger = businessTriggerRepository.save(new BusinessTrigger(
                asset,
                type,
                reason,
                urgent,
                userId
        ));

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.BUSINESS_TRIGGER_CREATED,
                userId,
                LocalDate.now()
        ));

        return BusinessTriggerResponse.from(trigger);
    }

    public void requireCanCreateBusinessTriggers(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager() && !user.getRole().isOperationalCoordinator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers and operational coordinators can create business triggers");
        }
    }

    private BusinessTrigger findTriggerOrThrow(Long id) {
        return businessTriggerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business trigger not found"));
    }

    private Asset findAssetOrThrow(Long assetId) {
        if (assetId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset is required");
        }
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset not found"));
    }

    private BusinessTriggerType validateType(BusinessTriggerType type) {
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trigger type is required");
        }
        return type;
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trigger reason is required");
        }
        return reason.trim();
    }

    private boolean resolveUrgent(BusinessTriggerType type, Boolean urgent) {
        if (type == BusinessTriggerType.EMERGENCY_EVENT) {
            return true;
        }
        return urgent != null && urgent;
    }
}

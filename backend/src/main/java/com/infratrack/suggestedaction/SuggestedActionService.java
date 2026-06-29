package com.infratrack.suggestedaction;

import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.suggestedaction.dto.SuggestedActionResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuggestedActionService {

    private final InspectionRepository inspectionRepository;
    private final SuggestedActionRepository suggestedActionRepository;
    private final InspectionAuthorizationService authorizationService;
    private final UserService userService;

    public SuggestedActionService(
            InspectionRepository inspectionRepository,
            SuggestedActionRepository suggestedActionRepository,
            InspectionAuthorizationService authorizationService,
            UserService userService) {
        this.inspectionRepository = inspectionRepository;
        this.suggestedActionRepository = suggestedActionRepository;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<SuggestedActionResponse> listSuggestedActions(
            Long inspectionId,
            Long userId,
            SuggestedActionStatus status,
            DecisionRuleActionType actionType) {
        requireVisibleInspection(inspectionId, userId);
        return suggestedActionRepository.findByInspectionWithOptionalFilters(inspectionId, status, actionType)
                .stream()
                .map(SuggestedActionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SuggestedActionResponse getSuggestedAction(
            Long inspectionId,
            Long suggestedActionId,
            Long userId) {
        requireVisibleInspection(inspectionId, userId);
        SuggestedAction action = suggestedActionRepository.findByIdAndInspection_Id(suggestedActionId, inspectionId)
                .orElseThrow(() -> new NotFoundException("Suggested action not found"));
        return SuggestedActionResponse.from(action);
    }

    private Inspection requireVisibleInspection(Long inspectionId, Long userId) {
        Inspection inspection = inspectionRepository.findWithEvaluationContextById(inspectionId)
                .orElseThrow(() -> new NotFoundException("Inspection not found"));
        User user = userService.getById(userId);
        authorizationService.requireCanViewInspection(user, inspection);
        return inspection;
    }
}

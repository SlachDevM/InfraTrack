package com.infratrack.mobile;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.mobile.dto.AssetContextAllowedActionsResponse;
import com.infratrack.mobile.dto.AssetContextResponse;
import com.infratrack.mobile.dto.AssetContextSummaryResponse;
import com.infratrack.mobile.dto.MobileAllowedActionsResponse;
import com.infratrack.mobile.dto.MobileAnswerResponse;
import com.infratrack.mobile.dto.MobileAssetDocumentSummaryResponse;
import com.infratrack.mobile.dto.MobileAssetLastInspectionResponse;
import com.infratrack.mobile.dto.MobileAssetLastMaintenanceResponse;
import com.infratrack.mobile.dto.MobileAssetPreventivePlanResponse;
import com.infratrack.mobile.dto.MobileAssetSummaryResponse;
import com.infratrack.mobile.dto.MobileDashboardResponse;
import com.infratrack.mobile.dto.MobileDecisionSummaryResponse;
import com.infratrack.mobile.dto.MobileInspectionBundleResponse;
import com.infratrack.mobile.dto.MobileInspectionDetailResponse;
import com.infratrack.mobile.dto.MobileInspectionSummaryResponse;
import com.infratrack.mobile.dto.MobileIssueSummaryResponse;
import com.infratrack.mobile.dto.MobileMaintenanceActivitySummaryResponse;
import com.infratrack.mobile.dto.MobileMeResponse;
import com.infratrack.mobile.dto.MobileQuestionResponse;
import com.infratrack.mobile.dto.MobileTemplateSummaryResponse;
import com.infratrack.mobile.dto.MobileWorkOrderAllowedActionsResponse;
import com.infratrack.mobile.dto.MobileWorkOrderBundleResponse;
import com.infratrack.mobile.dto.MobileWorkOrderDetailResponse;
import com.infratrack.mobile.dto.MobileWorkOrderSummaryResponse;
import com.infratrack.operationaldocument.OperationalDocument;
import com.infratrack.operationaldocument.OperationalDocumentService;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanRepository;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MobileService {

    private final MobileAuthorizationService authorizationService;
    private final UserService userService;
    private final AssetRepository assetRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionAnswerRepository inspectionAnswerRepository;
    private final MobileInspectionChecklistLoader checklistLoader;
    private final IssueRepository issueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final PreventiveMaintenancePlanRepository preventiveMaintenancePlanRepository;
    private final OperationalDocumentService operationalDocumentService;
    private final UserNameLookup userNameLookup;

    public MobileService(
            MobileAuthorizationService authorizationService,
            UserService userService,
            AssetRepository assetRepository,
            InspectionRepository inspectionRepository,
            InspectionAnswerRepository inspectionAnswerRepository,
            MobileInspectionChecklistLoader checklistLoader,
            IssueRepository issueRepository,
            WorkOrderRepository workOrderRepository,
            MaintenanceActivityRepository maintenanceActivityRepository,
            PreventiveMaintenancePlanRepository preventiveMaintenancePlanRepository,
            OperationalDocumentService operationalDocumentService,
            UserNameLookup userNameLookup) {
        this.authorizationService = authorizationService;
        this.userService = userService;
        this.assetRepository = assetRepository;
        this.inspectionRepository = inspectionRepository;
        this.inspectionAnswerRepository = inspectionAnswerRepository;
        this.checklistLoader = checklistLoader;
        this.issueRepository = issueRepository;
        this.workOrderRepository = workOrderRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.preventiveMaintenancePlanRepository = preventiveMaintenancePlanRepository;
        this.operationalDocumentService = operationalDocumentService;
        this.userNameLookup = userNameLookup;
    }

    @Transactional(readOnly = true)
    public MobileMeResponse getMe(Long userId) {
        User user = authorizationService.requireMobileUser(userId);
        return MobileMeResponse.from(user);
    }

    @Transactional(readOnly = true)
    public MobileDashboardResponse getDashboard(Long userId) {
        User user = authorizationService.requireMobileUser(userId);
        return buildDashboard(user);
    }

    @Transactional(readOnly = true)
    public MobileDashboardResponse buildDashboard(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        long assignedInspections = inspectionRepository.countByAssignedToUserIdAndStatus(
                user.getId(), InspectionStatus.ASSIGNED);
        long assignedWorkOrders = workOrderRepository.countByAssignedToUserIdAndStatus(
                user.getId(), WorkOrderStatus.ASSIGNED);
        long overdueInspections = inspectionRepository.countOverdueByAssignedUser(
                user.getId(), InspectionStatus.ASSIGNED, today);
        long inspectionsCompletedToday = inspectionRepository.countCompletedByUserBetween(
                user.getId(), dayStart, dayEnd);
        long maintenanceCompletedToday = maintenanceActivityRepository.countCompletedByUserBetween(
                user.getId(), dayStart, dayEnd);

        return new MobileDashboardResponse(
                assignedInspections,
                assignedWorkOrders,
                overdueInspections,
                0L,
                inspectionsCompletedToday + maintenanceCompletedToday);
    }

    @Transactional(readOnly = true)
    public List<MobileInspectionSummaryResponse> getMyInspections(Long userId) {
        User user = authorizationService.requireMobileUser(userId);
        List<Inspection> inspections = new ArrayList<>(loadScopedInspections(user));
        inspections.sort(inspectionListComparator(LocalDate.now()));
        return inspections.stream()
                .map(MobileInspectionSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Inspection> listScopedInspectionsForSync(User user) {
        return listScopedInspectionsForSync(user, null, null);
    }

    @Transactional(readOnly = true)
    public List<Inspection> listScopedInspectionsForSync(User user, Long updatedSinceMillis) {
        return listScopedInspectionsForSync(user, updatedSinceMillis, null);
    }

    @Transactional(readOnly = true)
    public List<Inspection> listScopedInspectionsForSync(
            User user, Long updatedSinceMillis, Long updatedUntilMillis) {
        authorizationService.requireMobileUser(user.getId());
        return loadScopedInspectionsForSync(user, updatedSinceMillis, updatedUntilMillis);
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> listScopedWorkOrdersForSync(User user) {
        return listScopedWorkOrdersForSync(user, null, null);
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> listScopedWorkOrdersForSync(User user, Long updatedSinceMillis) {
        return listScopedWorkOrdersForSync(user, updatedSinceMillis, null);
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> listScopedWorkOrdersForSync(
            User user, Long updatedSinceMillis, Long updatedUntilMillis) {
        authorizationService.requireMobileUser(user.getId());
        return loadScopedWorkOrdersForSync(user, updatedSinceMillis, updatedUntilMillis);
    }

    @Transactional(readOnly = true)
    public MobileInspectionBundleResponse getInspectionBundle(Long userId, Long inspectionId) {
        User user = authorizationService.requireMobileUser(userId);
        Inspection inspection = inspectionRepository.findMobileBundleById(inspectionId)
                .orElseThrow(() -> new NotFoundException("Inspection not found."));
        authorizationService.requireCanViewInspectionBundle(user, inspection);

        MobileTemplateSummaryResponse template = null;
        List<MobileQuestionResponse> questions = List.of();
        if (inspection.getInspectionTemplate() != null) {
            template = MobileTemplateSummaryResponse.from(inspection.getInspectionTemplate());
            questions = checklistLoader.loadMobileQuestions(inspection.getInspectionTemplate().getId());
        }

        List<MobileAnswerResponse> answers = inspectionAnswerRepository
                .findByInspectionIdOrderByQuestionDisplayOrder(inspectionId)
                .stream()
                .map(MobileAnswerResponse::from)
                .toList();

        boolean canComplete = authorizationService.canCompleteInspection(user, inspection);
        MobileAllowedActionsResponse allowedActions = new MobileAllowedActionsResponse(
                canComplete,
                canComplete,
                true);

        return new MobileInspectionBundleResponse(
                MobileInspectionDetailResponse.from(inspection),
                MobileAssetSummaryResponse.from(inspection.getAsset()),
                template,
                questions,
                answers,
                allowedActions);
    }

    @Transactional(readOnly = true)
    public List<MobileWorkOrderSummaryResponse> getMyWorkOrders(Long userId) {
        User user = authorizationService.requireMobileUser(userId);
        List<WorkOrder> workOrders = new ArrayList<>(loadScopedWorkOrders(user));
        workOrders.sort(workOrderListComparator());
        return workOrders.stream()
                .map(MobileWorkOrderSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MobileWorkOrderBundleResponse getWorkOrderBundle(Long userId, Long workOrderId) {
        User user = authorizationService.requireMobileUser(userId);
        WorkOrder workOrder = workOrderRepository.findMobileBundleById(workOrderId)
                .orElseThrow(() -> new NotFoundException("Work order not found."));
        authorizationService.requireCanViewWorkOrderBundle(user, workOrder);

        MobileIssueSummaryResponse issue = MobileIssueSummaryResponse.from(
                workOrder.getOperationalDecision().getIssue());
        MobileDecisionSummaryResponse decision = MobileDecisionSummaryResponse.from(
                workOrder.getOperationalDecision());

        MobileMaintenanceActivitySummaryResponse maintenanceActivity = maintenanceActivityRepository
                .findByWorkOrderId(workOrderId)
                .map(MobileMaintenanceActivitySummaryResponse::from)
                .orElse(null);

        boolean canComplete = authorizationService.canCompleteMaintenance(user, workOrder);
        MobileWorkOrderAllowedActionsResponse allowedActions = new MobileWorkOrderAllowedActionsResponse(
                canComplete,
                canComplete,
                true);

        return new MobileWorkOrderBundleResponse(
                MobileWorkOrderDetailResponse.from(workOrder),
                MobileAssetSummaryResponse.from(workOrder.getAsset()),
                issue,
                decision,
                maintenanceActivity,
                allowedActions);
    }

    @Transactional(readOnly = true)
    public AssetContextResponse getAssetContext(Long userId, String code) {
        User user = userService.getById(userId);
        Asset asset = findAssetByCodeOrThrow(code);
        return buildAssetContext(user, asset);
    }

    @Transactional(readOnly = true)
    public List<AssetContextResponse> buildAssetContextsForSync(User user, Collection<Long> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) {
            return List.of();
        }
        List<Asset> visibleAssets = assetRepository.findByIdIn(assetIds).stream()
                .filter(asset -> isAssetContextVisible(user, asset))
                .toList();
        if (visibleAssets.isEmpty()) {
            return List.of();
        }
        AssetContextBatch batch = loadAssetContextBatch(user, visibleAssets);
        return visibleAssets.stream()
                .map(asset -> assembleAssetContext(user, asset, batch))
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetContextResponse buildAssetContext(User user, Asset asset) {
        authorizationService.requireCanViewAssetContext(user, asset);
        AssetContextBatch batch = loadAssetContextBatch(user, List.of(asset));
        return assembleAssetContext(user, asset, batch);
    }

    private AssetContextResponse assembleAssetContext(User user, Asset asset, AssetContextBatch batch) {
        Long assetId = asset.getId();
        AssetContextAllowedActionsResponse allowedActions = new AssetContextAllowedActionsResponse(
                true,
                true,
                true,
                true,
                authorizationService.canCreateInspectionForAsset(user, asset),
                authorizationService.canCreateIssueForAsset(user, asset));

        return new AssetContextResponse(
                AssetContextSummaryResponse.from(asset),
                batch.lastInspectionByAssetId().get(assetId),
                batch.lastMaintenanceByAssetId().get(assetId),
                batch.preventivePlanByAssetId().get(assetId),
                batch.documentsByAssetId().getOrDefault(assetId, List.of()),
                batch.openIssuesByAssetId().getOrDefault(assetId, List.of()),
                batch.activeInspectionsByAssetId().getOrDefault(assetId, List.of()),
                batch.activeWorkOrdersByAssetId().getOrDefault(assetId, List.of()),
                allowedActions);
    }

    private AssetContextBatch loadAssetContextBatch(User user, List<Asset> assets) {
        List<Long> assetIds = assets.stream().map(Asset::getId).toList();

        Map<Long, List<MobileIssueSummaryResponse>> openIssuesByAssetId =
                issueRepository.findOpenByAssetIdIn(assetIds).stream()
                        .collect(Collectors.groupingBy(
                                issue -> issue.getAsset().getId(),
                                Collectors.mapping(MobileIssueSummaryResponse::from, Collectors.toList())));

        Map<Long, List<MobileInspectionSummaryResponse>> activeInspectionsByAssetId =
                inspectionRepository.findByAsset_IdInAndStatus(assetIds, InspectionStatus.ASSIGNED).stream()
                        .collect(Collectors.groupingBy(
                                inspection -> inspection.getAsset().getId(),
                                Collectors.mapping(MobileInspectionSummaryResponse::from, Collectors.toList())));

        List<WorkOrderStatus> activeWorkOrderStatuses = List.of(WorkOrderStatus.CREATED, WorkOrderStatus.ASSIGNED);
        Map<Long, List<MobileWorkOrderSummaryResponse>> activeWorkOrdersByAssetId =
                workOrderRepository.findByAsset_IdInAndStatusIn(assetIds, activeWorkOrderStatuses).stream()
                        .collect(Collectors.groupingBy(
                                workOrder -> workOrder.getAsset().getId(),
                                Collectors.mapping(MobileWorkOrderSummaryResponse::from, Collectors.toList())));

        Map<Long, Inspection> latestCompletedByAssetId = new HashMap<>();
        for (Inspection inspection : inspectionRepository.findByAsset_IdInAndStatus(
                assetIds, InspectionStatus.COMPLETED)) {
            Long assetId = inspection.getAsset().getId();
            Inspection current = latestCompletedByAssetId.get(assetId);
            if (current == null || isAfter(inspection.getCompletedAt(), current.getCompletedAt())) {
                latestCompletedByAssetId.put(assetId, inspection);
            }
        }
        Map<Long, MobileAssetLastInspectionResponse> lastInspectionByAssetId = new HashMap<>();
        for (Map.Entry<Long, Inspection> entry : latestCompletedByAssetId.entrySet()) {
            lastInspectionByAssetId.put(entry.getKey(), MobileAssetLastInspectionResponse.from(entry.getValue()));
        }

        Map<Long, MobileAssetLastMaintenanceResponse> lastMaintenanceByAssetId =
                loadLastMaintenanceByAssetId(assetIds);

        Map<Long, MobileAssetPreventivePlanResponse> preventivePlanByAssetId = new HashMap<>();
        for (var plan : preventiveMaintenancePlanRepository.findByAsset_IdInAndStatusOrderByCreatedAtDesc(
                assetIds, PreventiveMaintenancePlanStatus.ACTIVE)) {
            preventivePlanByAssetId.putIfAbsent(
                    plan.getAsset().getId(),
                    MobileAssetPreventivePlanResponse.from(plan));
        }

        Map<Long, List<MobileAssetDocumentSummaryResponse>> documentsByAssetId =
                loadAssetDocumentsBatch(user, assets);

        return new AssetContextBatch(
                openIssuesByAssetId,
                activeInspectionsByAssetId,
                activeWorkOrdersByAssetId,
                lastInspectionByAssetId,
                lastMaintenanceByAssetId,
                preventivePlanByAssetId,
                documentsByAssetId);
    }

    private Map<Long, MobileAssetLastMaintenanceResponse> loadLastMaintenanceByAssetId(List<Long> assetIds) {
        List<MaintenanceActivity> activities =
                maintenanceActivityRepository.findByAsset_IdInOrderByCompletedAtDesc(assetIds);
        if (activities.isEmpty()) {
            return Map.of();
        }

        Map<Long, MaintenanceActivity> latestByAssetId = new HashMap<>();
        for (MaintenanceActivity activity : activities) {
            latestByAssetId.putIfAbsent(activity.getAsset().getId(), activity);
        }

        Map<Long, String> performerNames = userNameLookup.resolveNames(
                latestByAssetId.values().stream()
                        .map(MaintenanceActivity::getPerformedByUserId)
                        .toList());

        Map<Long, MobileAssetLastMaintenanceResponse> result = new HashMap<>();
        for (Map.Entry<Long, MaintenanceActivity> entry : latestByAssetId.entrySet()) {
            MaintenanceActivity activity = entry.getValue();
            result.put(
                    entry.getKey(),
                    MobileAssetLastMaintenanceResponse.from(
                            activity,
                            performerNames.get(activity.getPerformedByUserId())));
        }
        return result;
    }

    private Map<Long, List<MobileAssetDocumentSummaryResponse>> loadAssetDocumentsBatch(
            User user, List<Asset> assets) {
        Map<Long, List<OperationalDocument>> documentsByAssetId =
                operationalDocumentService.listVisibleAssetOwnedDocumentsForAssets(user, assets);
        if (documentsByAssetId.isEmpty()) {
            return Map.of();
        }

        List<Long> uploaderIds = documentsByAssetId.values().stream()
                .flatMap(List::stream)
                .map(OperationalDocument::getUploadedByUserId)
                .distinct()
                .toList();
        Map<Long, String> uploaderNames = userNameLookup.resolveNames(uploaderIds);

        Map<Long, List<MobileAssetDocumentSummaryResponse>> result = new HashMap<>();
        for (Map.Entry<Long, List<OperationalDocument>> entry : documentsByAssetId.entrySet()) {
            result.put(
                    entry.getKey(),
                    entry.getValue().stream()
                            .map(document -> MobileAssetDocumentSummaryResponse.from(
                                    document,
                                    uploaderNames.get(document.getUploadedByUserId())))
                            .toList());
        }
        return result;
    }

    private static boolean isAfter(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return false;
        }
        if (right == null) {
            return true;
        }
        return left.isAfter(right);
    }

    private record AssetContextBatch(
            Map<Long, List<MobileIssueSummaryResponse>> openIssuesByAssetId,
            Map<Long, List<MobileInspectionSummaryResponse>> activeInspectionsByAssetId,
            Map<Long, List<MobileWorkOrderSummaryResponse>> activeWorkOrdersByAssetId,
            Map<Long, MobileAssetLastInspectionResponse> lastInspectionByAssetId,
            Map<Long, MobileAssetLastMaintenanceResponse> lastMaintenanceByAssetId,
            Map<Long, MobileAssetPreventivePlanResponse> preventivePlanByAssetId,
            Map<Long, List<MobileAssetDocumentSummaryResponse>> documentsByAssetId) {
    }

    private Asset findAssetByCodeOrThrow(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessValidationException("Asset code is required");
        }
        return assetRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new NotFoundException("Asset not found"));
    }

    private boolean isAssetContextVisible(User user, Asset asset) {
        try {
            authorizationService.requireCanViewAssetContext(user, asset);
            return true;
        } catch (ForbiddenOperationException ex) {
            return false;
        }
    }

    private List<Inspection> loadScopedInspections(User user) {
        if (user.getRole().isAdministrator()) {
            return inspectionRepository.findByStatus(InspectionStatus.ASSIGNED);
        }
        if (user.getRole().isManager()) {
            if (user.getDepartment() == null) {
                return List.of();
            }
            return inspectionRepository.findByAsset_Department_Id(user.getDepartment().getId())
                    .stream()
                    .filter(inspection -> inspection.getStatus() == InspectionStatus.ASSIGNED)
                    .toList();
        }
        return inspectionRepository.findByAssignedToUserId(user.getId());
    }

    private List<Inspection> loadScopedInspectionsForSync(
            User user, Long updatedSinceMillis, Long updatedUntilMillis) {
        if (user.getRole().isAdministrator()) {
            if (updatedSinceMillis == null) {
                if (updatedUntilMillis == null) {
                    return inspectionRepository.findByStatus(InspectionStatus.ASSIGNED);
                }
                return inspectionRepository.findByStatusAndUpdatedAtLessThanEqual(
                        InspectionStatus.ASSIGNED, updatedUntilMillis);
            }
            if (updatedUntilMillis == null) {
                return inspectionRepository.findByStatusAndUpdatedAtGreaterThanEqual(
                        InspectionStatus.ASSIGNED, updatedSinceMillis);
            }
            return inspectionRepository.findByStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                    InspectionStatus.ASSIGNED, updatedSinceMillis, updatedUntilMillis);
        }
        if (user.getRole().isManager()) {
            if (user.getDepartment() == null) {
                return List.of();
            }
            Long departmentId = user.getDepartment().getId();
            if (updatedSinceMillis == null) {
                if (updatedUntilMillis == null) {
                    return inspectionRepository.findByAsset_Department_IdAndStatus(
                            departmentId, InspectionStatus.ASSIGNED);
                }
                return inspectionRepository.findByAsset_Department_IdAndStatusAndUpdatedAtLessThanEqual(
                        departmentId, InspectionStatus.ASSIGNED, updatedUntilMillis);
            }
            if (updatedUntilMillis == null) {
                return inspectionRepository.findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqual(
                        departmentId, InspectionStatus.ASSIGNED, updatedSinceMillis);
            }
            return inspectionRepository.findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                    departmentId, InspectionStatus.ASSIGNED, updatedSinceMillis, updatedUntilMillis);
        }
        if (updatedSinceMillis == null) {
            if (updatedUntilMillis == null) {
                return inspectionRepository.findByAssignedToUserId(user.getId());
            }
            return inspectionRepository.findByAssignedToUserIdAndUpdatedAtLessThanEqual(
                    user.getId(), updatedUntilMillis);
        }
        if (updatedUntilMillis == null) {
            return inspectionRepository.findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(
                    user.getId(), updatedSinceMillis);
        }
        return inspectionRepository.findByAssignedToUserIdAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                user.getId(), updatedSinceMillis, updatedUntilMillis);
    }

    private List<WorkOrder> loadScopedWorkOrdersForSync(
            User user, Long updatedSinceMillis, Long updatedUntilMillis) {
        if (user.getRole().isAdministrator()) {
            if (updatedSinceMillis == null) {
                if (updatedUntilMillis == null) {
                    return workOrderRepository.findByStatus(WorkOrderStatus.ASSIGNED);
                }
                return workOrderRepository.findByStatusAndUpdatedAtLessThanEqual(
                        WorkOrderStatus.ASSIGNED, updatedUntilMillis);
            }
            if (updatedUntilMillis == null) {
                return workOrderRepository.findByStatusAndUpdatedAtGreaterThanEqual(
                        WorkOrderStatus.ASSIGNED, updatedSinceMillis);
            }
            return workOrderRepository.findByStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                    WorkOrderStatus.ASSIGNED, updatedSinceMillis, updatedUntilMillis);
        }
        if (user.getRole().isManager()) {
            if (user.getDepartment() == null) {
                return List.of();
            }
            Long departmentId = user.getDepartment().getId();
            if (updatedSinceMillis == null) {
                if (updatedUntilMillis == null) {
                    return workOrderRepository.findByAsset_Department_IdAndStatus(
                            departmentId, WorkOrderStatus.ASSIGNED);
                }
                return workOrderRepository.findByAsset_Department_IdAndStatusAndUpdatedAtLessThanEqual(
                        departmentId, WorkOrderStatus.ASSIGNED, updatedUntilMillis);
            }
            if (updatedUntilMillis == null) {
                return workOrderRepository.findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqual(
                        departmentId, WorkOrderStatus.ASSIGNED, updatedSinceMillis);
            }
            return workOrderRepository.findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                    departmentId, WorkOrderStatus.ASSIGNED, updatedSinceMillis, updatedUntilMillis);
        }
        if (updatedSinceMillis == null) {
            if (updatedUntilMillis == null) {
                return workOrderRepository.findByAssignedToUserId(user.getId());
            }
            return workOrderRepository.findByAssignedToUserIdAndUpdatedAtLessThanEqual(
                    user.getId(), updatedUntilMillis);
        }
        if (updatedUntilMillis == null) {
            return workOrderRepository.findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(
                    user.getId(), updatedSinceMillis);
        }
        return workOrderRepository.findByAssignedToUserIdAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                user.getId(), updatedSinceMillis, updatedUntilMillis);
    }

    private List<WorkOrder> loadScopedWorkOrders(User user) {
        if (user.getRole().isAdministrator()) {
            return workOrderRepository.findByStatus(WorkOrderStatus.ASSIGNED);
        }
        if (user.getRole().isManager()) {
            if (user.getDepartment() == null) {
                return List.of();
            }
            return workOrderRepository.findByAsset_Department_Id(user.getDepartment().getId())
                    .stream()
                    .filter(workOrder -> workOrder.getStatus() == WorkOrderStatus.ASSIGNED)
                    .toList();
        }
        return workOrderRepository.findByAssignedToUserId(user.getId());
    }

    static Comparator<Inspection> inspectionListComparator(LocalDate today) {
        return Comparator
                .comparing((Inspection inspection) -> !isOverdue(inspection, today))
                .thenComparing(
                        Inspection::getExpectedCompletionDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(inspection -> priorityRank(inspection.getPriority()));
    }

    static Comparator<WorkOrder> workOrderListComparator() {
        return Comparator
                .comparing((WorkOrder workOrder) -> workOrder.getStatus() != WorkOrderStatus.ASSIGNED)
                .thenComparing(WorkOrder::getAssignedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(workOrder -> workOrderPriorityRank(workOrder.getPriority()));
    }

    private static boolean isOverdue(Inspection inspection, LocalDate today) {
        return inspection.getStatus() == InspectionStatus.ASSIGNED
                && inspection.getExpectedCompletionDate() != null
                && inspection.getExpectedCompletionDate().isBefore(today);
    }

    private static int priorityRank(InspectionPriority priority) {
        if (priority == null) {
            return Integer.MAX_VALUE;
        }
        return switch (priority) {
            case URGENT -> 0;
            case HIGH -> 1;
            case NORMAL -> 2;
            case LOW -> 3;
        };
    }

    private static int workOrderPriorityRank(WorkOrderPriority priority) {
        if (priority == null) {
            return Integer.MAX_VALUE;
        }
        return switch (priority) {
            case URGENT -> 0;
            case HIGH -> 1;
            case NORMAL -> 2;
            case LOW -> 3;
        };
    }
}

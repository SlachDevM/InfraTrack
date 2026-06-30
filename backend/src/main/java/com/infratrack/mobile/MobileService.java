package com.infratrack.mobile;

import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.mobile.dto.MobileAllowedActionsResponse;
import com.infratrack.mobile.dto.MobileAnswerResponse;
import com.infratrack.mobile.dto.MobileAssetSummaryResponse;
import com.infratrack.mobile.dto.MobileChoiceResponse;
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
import com.infratrack.mobile.dto.MobileWorkOrderBundleResponse;
import com.infratrack.mobile.dto.MobileWorkOrderDetailResponse;
import com.infratrack.mobile.dto.MobileWorkOrderSummaryResponse;
import com.infratrack.user.User;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MobileService {

    private final MobileAuthorizationService authorizationService;
    private final InspectionRepository inspectionRepository;
    private final InspectionAnswerRepository inspectionAnswerRepository;
    private final InspectionTemplateQuestionRepository questionRepository;
    private final InspectionTemplateQuestionChoiceRepository choiceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;

    public MobileService(
            MobileAuthorizationService authorizationService,
            InspectionRepository inspectionRepository,
            InspectionAnswerRepository inspectionAnswerRepository,
            InspectionTemplateQuestionRepository questionRepository,
            InspectionTemplateQuestionChoiceRepository choiceRepository,
            WorkOrderRepository workOrderRepository,
            MaintenanceActivityRepository maintenanceActivityRepository) {
        this.authorizationService = authorizationService;
        this.inspectionRepository = inspectionRepository;
        this.inspectionAnswerRepository = inspectionAnswerRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
    }

    @Transactional(readOnly = true)
    public MobileMeResponse getMe(Long userId) {
        User user = authorizationService.requireMobileUser(userId);
        return MobileMeResponse.from(user);
    }

    @Transactional(readOnly = true)
    public MobileDashboardResponse getDashboard(Long userId) {
        User user = authorizationService.requireMobileUser(userId);
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
    public MobileInspectionBundleResponse getInspectionBundle(Long userId, Long inspectionId) {
        User user = authorizationService.requireMobileUser(userId);
        Inspection inspection = inspectionRepository.findMobileBundleById(inspectionId)
                .orElseThrow(() -> new NotFoundException("Inspection not found."));
        authorizationService.requireCanViewInspectionBundle(user, inspection);

        MobileTemplateSummaryResponse template = null;
        List<MobileQuestionResponse> questions = List.of();
        if (inspection.getInspectionTemplate() != null) {
            template = MobileTemplateSummaryResponse.from(inspection.getInspectionTemplate());
            questions = loadTemplateQuestions(inspection.getInspectionTemplate().getId());
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
        MobileAllowedActionsResponse allowedActions = new MobileAllowedActionsResponse(
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

    private List<MobileQuestionResponse> loadTemplateQuestions(Long templateId) {
        List<InspectionTemplateQuestion> questions = questionRepository
                .findByInspectionTemplateIdOrderByDisplayOrderAsc(templateId)
                .stream()
                .filter(InspectionTemplateQuestion::isActive)
                .toList();
        if (questions.isEmpty()) {
            return List.of();
        }

        List<Long> questionIds = questions.stream().map(InspectionTemplateQuestion::getId).toList();
        Map<Long, List<MobileChoiceResponse>> choicesByQuestionId = choiceRepository
                .findByQuestionIdInOrderByQuestionIdAscDisplayOrderAsc(questionIds)
                .stream()
                .filter(InspectionTemplateQuestionChoice::isActive)
                .collect(Collectors.groupingBy(
                        choice -> choice.getQuestion().getId(),
                        Collectors.mapping(MobileChoiceResponse::from, Collectors.toList())));

        List<MobileQuestionResponse> responses = new ArrayList<>();
        for (InspectionTemplateQuestion question : questions) {
            List<MobileChoiceResponse> choices = choicesByQuestionId.getOrDefault(
                    question.getId(), List.of());
            responses.add(MobileQuestionResponse.from(question, choices));
        }
        return responses;
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

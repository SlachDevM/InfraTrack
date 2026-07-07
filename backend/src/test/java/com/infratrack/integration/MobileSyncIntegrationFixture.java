package com.infratrack.integration;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Shared database fixtures for mobile sync integration tests (M6.5-STAB-2).
 */
final class MobileSyncIntegrationFixture {

    private final DepartmentRepository departmentRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final BusinessTriggerRepository businessTriggerRepository;
    private final InspectionRepository inspectionRepository;
    private final IssueRepository issueRepository;
    private final OperationalDecisionRepository operationalDecisionRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PasswordEncoder passwordEncoder;

    MobileSyncIntegrationFixture(
            DepartmentRepository departmentRepository,
            AssetCategoryRepository assetCategoryRepository,
            UserRepository userRepository,
            AssetRepository assetRepository,
            BusinessTriggerRepository businessTriggerRepository,
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            WorkOrderRepository workOrderRepository,
            PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.businessTriggerRepository = businessTriggerRepository;
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.workOrderRepository = workOrderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    Department createDepartment(String name) {
        return departmentRepository.save(new Department(name + "-" + System.nanoTime()));
    }

    AssetCategory createCategory() {
        return assetCategoryRepository.save(new AssetCategory("Sync Cat " + System.nanoTime()));
    }

    User createUser(String emailPrefix, String name, UserRole role, Department department) {
        User user = new User(
                emailPrefix + "+" + System.nanoTime() + "@sync.test",
                passwordEncoder.encode("Secret1!"),
                name,
                role);
        user.setEnabled(true);
        user.setDepartment(department);
        return userRepository.save(user);
    }

    Asset createAsset(String name, Department department, AssetCategory category, Long registeredByUserId) {
        return assetRepository.save(new Asset(
                name,
                department,
                category,
                "Sync location",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 1, 15),
                registeredByUserId));
    }

    BusinessTrigger createTrigger(Asset asset, Long createdByUserId) {
        return businessTriggerRepository.save(new BusinessTrigger(
                asset,
                BusinessTriggerType.SCHEDULED_INSPECTION,
                "Scheduled inspection",
                false,
                createdByUserId));
    }

    Inspection createAssignedInspection(
            Asset asset,
            BusinessTrigger trigger,
            Long assignedToUserId,
            Long assignedByUserId) {
        return inspectionRepository.save(new Inspection(
                asset,
                trigger,
                assignedToUserId,
                assignedByUserId,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(14)));
    }

    WorkOrder createAssignedWorkOrder(
            Asset asset,
            Long assignedToUserId,
            Long assignedByUserId,
            WorkType workType,
            String description) {
        Inspection inspection = createAssignedInspection(
                asset,
                createTrigger(asset, assignedByUserId),
                assignedToUserId,
                assignedByUserId);
        inspection.complete(
                com.infratrack.inspection.PhysicalCondition.FAIR,
                "Issue identified during sync fixture setup",
                true,
                LocalDateTime.now().minusHours(2),
                assignedToUserId);
        inspectionRepository.save(inspection);

        Issue issue = issueRepository.save(new Issue(
                inspection,
                asset,
                "Sync fixture issue for " + description,
                IssueSeverity.MEDIUM,
                assignedByUserId,
                LocalDateTime.now().minusHours(1)));

        OperationalDecisionOutcome outcome = workType == WorkType.CONTRACTOR_WORK
                ? OperationalDecisionOutcome.CONTRACTOR_WORK
                : OperationalDecisionOutcome.INTERNAL_MAINTENANCE;
        OperationalDecision decision = operationalDecisionRepository.save(new OperationalDecision(
                issue,
                asset,
                outcome,
                "Operational decision for sync fixture",
                assignedByUserId,
                LocalDateTime.now().minusMinutes(45)));

        WorkOrder workOrder = new WorkOrder(
                decision,
                asset,
                workType,
                description,
                WorkOrderPriority.NORMAL,
                assignedByUserId,
                LocalDateTime.now().minusMinutes(30));
        workOrder.assign(assignedToUserId, assignedByUserId, LocalDateTime.now().minusMinutes(20));
        return workOrderRepository.save(workOrder);
    }

    WorkOrder reloadWorkOrder(Long workOrderId) {
        return workOrderRepository.findById(workOrderId).orElseThrow();
    }

    Inspection reloadInspection(Long inspectionId) {
        return inspectionRepository.findById(inspectionId).orElseThrow();
    }

    void completeWorkOrder(Long workOrderId) {
        WorkOrder workOrder = reloadWorkOrder(workOrderId);
        workOrder.complete();
        workOrderRepository.save(workOrder);
    }

    static boolean deltaContainsInspectionId(
            com.fasterxml.jackson.databind.JsonNode response, long inspectionId) {
        for (com.fasterxml.jackson.databind.JsonNode item : response.path("delta").path("inspections")) {
            if (item.path("id").asLong() == inspectionId) {
                return true;
            }
        }
        return false;
    }

    static boolean deltaContainsWorkOrderId(
            com.fasterxml.jackson.databind.JsonNode response, long workOrderId) {
        for (com.fasterxml.jackson.databind.JsonNode item : response.path("delta").path("workOrders")) {
            if (item.path("workOrderId").asLong() == workOrderId) {
                return true;
            }
        }
        return false;
    }

    static boolean deltaContainsAssetId(com.fasterxml.jackson.databind.JsonNode response, long assetId) {
        for (com.fasterxml.jackson.databind.JsonNode item : response.path("delta").path("assets")) {
            if (item.path("asset").path("id").asLong() == assetId) {
                return true;
            }
        }
        return false;
    }
}

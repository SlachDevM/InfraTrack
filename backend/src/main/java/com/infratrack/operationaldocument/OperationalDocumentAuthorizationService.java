package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Validates upload permissions and resolves operational document ownership context.
 */
@Service
public class OperationalDocumentAuthorizationService {

    private final InspectionRepository inspectionRepository;
    private final IssueRepository issueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final DelegatedAuthorityService delegatedAuthorityService;

    public OperationalDocumentAuthorizationService(
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            WorkOrderRepository workOrderRepository,
            MaintenanceActivityRepository maintenanceActivityRepository,
            DelegatedAuthorityService delegatedAuthorityService) {
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.workOrderRepository = workOrderRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.delegatedAuthorityService = delegatedAuthorityService;
    }

    public void requireDeleteAuthorized(User user, OperationalDocumentOwnerContext ownerContext) {
        requireUploadAuthorized(user, ownerContext);
    }

    public void requireUploadAuthorized(User user, OperationalDocumentOwnerContext ownerContext) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbiddenUpload();
        }
        if (role.isAdministrator()) {
            throw new ForbiddenOperationException(
                    "Administrators cannot upload operational evidence");
        }

        requireAssetDepartmentAuthorized(user, ownerContext.asset());

        switch (role) {
            case MANAGER, OPERATIONAL_COORDINATOR -> {
                // Department ownership verified above.
            }
            case FIELD_EMPLOYEE, CONTRACTOR -> requireFieldUploadAuthorized(user, ownerContext);
            default -> throw forbiddenUpload();
        }
    }

    public void requireAssetDepartmentAuthorized(User user, Asset asset) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbiddenUpload();
        }
        if (role.isManager()) {
            if (!delegatedAuthorityService.canManagerActForAssetDepartment(
                    user, asset.getDepartment(), LocalDateTime.now())) {
                throw new ForbiddenOperationException(
                        "You may only upload operational documents for assets in your own department.");
            }
            return;
        }
        if (role.isOperationalCoordinator() || role.isFieldEmployee() || role.isContractor()) {
            requireSameDepartment(user, asset);
            return;
        }
        throw forbiddenUpload();
    }

    private void requireSameDepartment(User user, Asset asset) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (userDepartment == null || assetDepartment == null
                || !userDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "You may only upload operational documents for assets in your own department.");
        }
    }

    private ForbiddenOperationException forbiddenUpload() {
        return new ForbiddenOperationException("Unauthorized to upload operational evidence");
    }

    private void requireFieldUploadAuthorized(User user, OperationalDocumentOwnerContext ownerContext) {
        switch (ownerContext.ownerType()) {
            case ASSET, OPERATIONAL_DECISION, COMPLETION_REVIEW -> throw new ForbiddenOperationException(
                    "Unauthorized to upload operational evidence for this context");
            case INSPECTION -> {
                Inspection inspection = inspectionRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(inspection.getAssignedToUserId(), user.getId())
                        && !Objects.equals(inspection.getCompletedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
            case ISSUE -> {
                Issue issue = issueRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(issue.getRecordedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
            case WORK_ORDER -> {
                WorkOrder workOrder = workOrderRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(workOrder.getAssignedToUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
            case MAINTENANCE_ACTIVITY -> {
                MaintenanceActivity maintenanceActivity = maintenanceActivityRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(maintenanceActivity.getPerformedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
        }
    }
}

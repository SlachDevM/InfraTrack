package com.infratrack.operationaldocument;

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

import java.util.Objects;

@Service
public class OperationalDocumentAuthorizationService {

    private final InspectionRepository inspectionRepository;
    private final IssueRepository issueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;

    public OperationalDocumentAuthorizationService(
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            WorkOrderRepository workOrderRepository,
            MaintenanceActivityRepository maintenanceActivityRepository) {
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.workOrderRepository = workOrderRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
    }

    public void requireUploadAuthorized(User user, OperationalDocumentOwnerContext ownerContext) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbiddenUpload();
        }

        switch (role) {
            case ADMINISTRATOR -> throw new ForbiddenOperationException(
                    "Administrators cannot upload operational evidence");
            case MANAGER, OPERATIONAL_COORDINATOR -> {
                // UC-012: Managers and Operational Coordinators may upload in any context.
            }
            case FIELD_EMPLOYEE, CONTRACTOR -> requireFieldUploadAuthorized(user, ownerContext);
            default -> throw forbiddenUpload();
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

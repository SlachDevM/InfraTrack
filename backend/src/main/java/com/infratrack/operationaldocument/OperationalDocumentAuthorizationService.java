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
import com.infratrack.messages.OperationalEvidenceMessages;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Validates upload, download and delete permissions and resolves operational document ownership context.
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

    public void requireDownloadAuthorized(User user, OperationalDocumentOwnerContext ownerContext) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbiddenDownload();
        }
        if (role.isAdministrator()) {
            return;
        }

        requireAssetDepartmentAccess(
                user,
                ownerContext.asset(),
                "You may only download operational documents for assets in your own department.",
                OperationalEvidenceMessages.UNAUTHORIZED_DOWNLOAD_OPERATIONAL_EVIDENCE);

        switch (role) {
            case MANAGER, OPERATIONAL_COORDINATOR -> {
                // Department ownership verified above.
            }
            case FIELD_EMPLOYEE, CONTRACTOR -> requireFieldOwnerAccess(
                    user,
                    ownerContext,
                    OperationalEvidenceMessages.UNAUTHORIZED_DOWNLOAD_OPERATIONAL_EVIDENCE_CONTEXT);
            default -> throw forbiddenDownload();
        }
    }

    public void requireAssetDepartmentDownloadAuthorized(User user, Asset asset) {
        requireAssetDepartmentAccess(
                user,
                asset,
                "You may only download operational documents for assets in your own department.",
                OperationalEvidenceMessages.UNAUTHORIZED_DOWNLOAD_OPERATIONAL_EVIDENCE);
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
        requireAssetDepartmentAccess(
                user,
                asset,
                "You may only upload operational documents for assets in your own department.",
                OperationalEvidenceMessages.UNAUTHORIZED_UPLOAD_OPERATIONAL_EVIDENCE);
    }

    private void requireAssetDepartmentAccess(
            User user,
            Asset asset,
            String crossDepartmentMessage,
            String roleDeniedMessage) {
        UserRole role = user.getRole();
        if (role == null) {
            throw new ForbiddenOperationException(roleDeniedMessage);
        }
        if (role.isManager()) {
            if (!delegatedAuthorityService.canManagerActForAssetDepartment(
                    user, asset.getDepartment(), LocalDateTime.now())) {
                throw new ForbiddenOperationException(crossDepartmentMessage);
            }
            return;
        }
        if (role.isOperationalCoordinator() || role.isFieldEmployee() || role.isContractor()) {
            requireSameDepartment(user, asset, crossDepartmentMessage);
            return;
        }
        throw new ForbiddenOperationException(roleDeniedMessage);
    }

    private void requireSameDepartment(User user, Asset asset, String crossDepartmentMessage) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (userDepartment == null || assetDepartment == null
                || !userDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(crossDepartmentMessage);
        }
    }

    private ForbiddenOperationException forbiddenUpload() {
        return new ForbiddenOperationException(OperationalEvidenceMessages.UNAUTHORIZED_UPLOAD_OPERATIONAL_EVIDENCE);
    }

    private ForbiddenOperationException forbiddenDownload() {
        return new ForbiddenOperationException(OperationalEvidenceMessages.UNAUTHORIZED_DOWNLOAD_OPERATIONAL_EVIDENCE);
    }

    private void requireFieldUploadAuthorized(User user, OperationalDocumentOwnerContext ownerContext) {
        requireFieldOwnerAccess(
                user,
                ownerContext,
                OperationalEvidenceMessages.UNAUTHORIZED_UPLOAD_OPERATIONAL_EVIDENCE_CONTEXT);
    }

    private void requireFieldOwnerAccess(
            User user,
            OperationalDocumentOwnerContext ownerContext,
            String contextDeniedMessage) {
        switch (ownerContext.ownerType()) {
            case ASSET, OPERATIONAL_DECISION, COMPLETION_REVIEW -> throw new ForbiddenOperationException(
                    contextDeniedMessage);
            case INSPECTION -> {
                Inspection inspection = inspectionRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException(OperationalEvidenceMessages.OPERATIONAL_OWNER_NOT_FOUND));
                if (!Objects.equals(inspection.getAssignedToUserId(), user.getId())
                        && !Objects.equals(inspection.getCompletedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(contextDeniedMessage);
                }
            }
            case ISSUE -> {
                Issue issue = issueRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException(OperationalEvidenceMessages.OPERATIONAL_OWNER_NOT_FOUND));
                if (!Objects.equals(issue.getRecordedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(contextDeniedMessage);
                }
            }
            case WORK_ORDER -> {
                WorkOrder workOrder = workOrderRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException(OperationalEvidenceMessages.OPERATIONAL_OWNER_NOT_FOUND));
                if (!Objects.equals(workOrder.getAssignedToUserId(), user.getId())) {
                    throw new ForbiddenOperationException(contextDeniedMessage);
                }
            }
            case MAINTENANCE_ACTIVITY -> {
                MaintenanceActivity maintenanceActivity = maintenanceActivityRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException(OperationalEvidenceMessages.OPERATIONAL_OWNER_NOT_FOUND));
                if (!Objects.equals(maintenanceActivity.getPerformedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(contextDeniedMessage);
                }
            }
        }
    }
}

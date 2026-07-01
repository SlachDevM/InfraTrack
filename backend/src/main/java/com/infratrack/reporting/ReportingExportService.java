package com.infratrack.reporting;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.preventivemaintenance.PreventiveExecutionCandidate;
import com.infratrack.preventivemaintenance.PreventiveExecutionCandidateRepository;
import com.infratrack.user.UserNameLookup;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportingExportService {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ReportingAuthorizationService authorizationService;
    private final AssetRepository assetRepository;
    private final InspectionRepository inspectionRepository;
    private final IssueRepository issueRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PreventiveExecutionCandidateRepository preventiveExecutionCandidateRepository;
    private final OperationalDecisionRepository operationalDecisionRepository;
    private final UserNameLookup userNameLookup;

    public ReportingExportService(
            ReportingAuthorizationService authorizationService,
            AssetRepository assetRepository,
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            WorkOrderRepository workOrderRepository,
            PreventiveExecutionCandidateRepository preventiveExecutionCandidateRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            UserNameLookup userNameLookup) {
        this.authorizationService = authorizationService;
        this.assetRepository = assetRepository;
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.workOrderRepository = workOrderRepository;
        this.preventiveExecutionCandidateRepository = preventiveExecutionCandidateRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.userNameLookup = userNameLookup;
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportAssets(Long userId, Long from, Long to) {
        ExportScope scope = authorizationService.resolveScope(userId);
        List<Asset> assets = assetRepository.findForExport(scope.departmentId(), from, to);
        List<String> headers = List.of(
                "Asset ID", "Asset Name", "Category", "Department", "Location", "Status", "Created At");
        List<List<String>> rows = assets.stream()
                .map(asset -> List.of(
                        CsvExportWriter.cell(asset.getId()),
                        CsvExportWriter.cell(asset.getName()),
                        CsvExportWriter.cell(categoryName(asset)),
                        CsvExportWriter.cell(departmentName(asset)),
                        CsvExportWriter.cell(asset.getLocation()),
                        CsvExportWriter.cell(asset.getStatus()),
                        formatEpochMillis(asset.getCreatedAt())))
                .toList();
        return buildResponse(ExportType.ASSETS, headers, rows);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportInspections(Long userId, Long from, Long to) {
        ExportScope scope = authorizationService.resolveScope(userId);
        List<Inspection> inspections = inspectionRepository.findForExport(scope.departmentId(), from, to);
        Map<Long, String> assigneeNames = userNameLookup.resolveNames(inspections.stream()
                .map(Inspection::getAssignedToUserId)
                .collect(Collectors.toSet()));
        List<String> headers = List.of(
                "Inspection ID", "Asset Name", "Department", "Status", "Priority", "Assigned To",
                "Expected Completion Date", "Completed At", "Template", "Issue Identified");
        List<List<String>> rows = inspections.stream()
                .map(inspection -> List.of(
                        CsvExportWriter.cell(inspection.getId()),
                        CsvExportWriter.cell(inspection.getAsset().getName()),
                        CsvExportWriter.cell(departmentName(inspection.getAsset())),
                        CsvExportWriter.cell(inspection.getStatus()),
                        CsvExportWriter.cell(inspection.getPriority()),
                        CsvExportWriter.cell(assigneeNames.get(inspection.getAssignedToUserId())),
                        formatLocalDate(inspection.getExpectedCompletionDate()),
                        formatLocalDateTime(inspection.getCompletedAt()),
                        CsvExportWriter.cell(templateName(inspection)),
                        CsvExportWriter.cell(inspection.isIssueIdentified())))
                .toList();
        return buildResponse(ExportType.INSPECTIONS, headers, rows);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportIssues(Long userId, Long from, Long to) {
        ExportScope scope = authorizationService.resolveScope(userId);
        LocalDateTime fromDateTime = toLocalDateTime(from);
        LocalDateTime toDateTime = toLocalDateTimeExclusive(to);
        List<Issue> issues = issueRepository.findForExport(scope.departmentId(), fromDateTime, toDateTime);
        Set<Long> resolvedIssueIds = issues.isEmpty()
                ? Set.of()
                : operationalDecisionRepository.findResolvedIssueIds(
                        issues.stream().map(Issue::getId).toList());
        Map<Long, String> recorderNames = userNameLookup.resolveNames(issues.stream()
                .map(Issue::getRecordedByUserId)
                .collect(Collectors.toSet()));
        List<String> headers = List.of(
                "Issue ID", "Asset Name", "Department", "Issue Type", "Severity", "Description",
                "Recorded By", "Recorded At", "Resolved");
        List<List<String>> rows = issues.stream()
                .map(issue -> List.of(
                        CsvExportWriter.cell(issue.getId()),
                        CsvExportWriter.cell(issue.getAsset().getName()),
                        CsvExportWriter.cell(departmentName(issue.getAsset())),
                        CsvExportWriter.cell(issue.getIssueType()),
                        CsvExportWriter.cell(issue.getSeverity()),
                        CsvExportWriter.cell(issue.getDescription()),
                        CsvExportWriter.cell(recorderNames.get(issue.getRecordedByUserId())),
                        formatLocalDateTime(issue.getRecordedAt()),
                        CsvExportWriter.cell(resolvedIssueIds.contains(issue.getId()))))
                .toList();
        return buildResponse(ExportType.ISSUES, headers, rows);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportWorkOrders(Long userId, Long from, Long to) {
        ExportScope scope = authorizationService.resolveScope(userId);
        List<WorkOrder> workOrders = workOrderRepository.findForExport(scope.departmentId(), from, to);
        Map<Long, String> assigneeNames = userNameLookup.resolveNames(workOrders.stream()
                .map(WorkOrder::getAssignedToUserId)
                .collect(Collectors.toSet()));
        List<String> headers = List.of(
                "Work Order ID", "Asset Name", "Department", "Status", "Priority", "Assigned To",
                "Description", "Created At", "Updated At");
        List<List<String>> rows = workOrders.stream()
                .map(workOrder -> List.of(
                        CsvExportWriter.cell(workOrder.getId()),
                        CsvExportWriter.cell(workOrder.getAsset().getName()),
                        CsvExportWriter.cell(departmentName(workOrder.getAsset())),
                        CsvExportWriter.cell(workOrder.getStatus()),
                        CsvExportWriter.cell(workOrder.getPriority()),
                        CsvExportWriter.cell(assigneeNames.get(workOrder.getAssignedToUserId())),
                        CsvExportWriter.cell(workOrder.getDescription()),
                        formatEpochMillis(workOrder.getCreatedAt()),
                        formatEpochMillis(workOrder.getUpdatedAt())))
                .toList();
        return buildResponse(ExportType.WORK_ORDERS, headers, rows);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportPreventiveCandidates(Long userId, Long from, Long to) {
        ExportScope scope = authorizationService.resolveScope(userId);
        List<PreventiveExecutionCandidate> candidates = preventiveExecutionCandidateRepository.findForExport(
                scope.departmentId(), from, to);
        List<String> headers = List.of(
                "Candidate ID", "Plan Code", "Asset Name", "Department", "Status", "Target Action",
                "Trigger Type", "Evaluated At", "Decision Date", "Created Inspection ID");
        List<List<String>> rows = candidates.stream()
                .map(candidate -> List.of(
                        CsvExportWriter.cell(candidate.getId()),
                        CsvExportWriter.cell(candidate.getPlanCodeSnapshot()),
                        CsvExportWriter.cell(candidate.getAsset().getName()),
                        CsvExportWriter.cell(departmentName(candidate.getAsset())),
                        CsvExportWriter.cell(candidate.getCandidateStatus()),
                        CsvExportWriter.cell(candidate.getTargetActionSnapshot()),
                        CsvExportWriter.cell(candidate.getTriggerType()),
                        formatEpochMillis(candidate.getEvaluatedAt()),
                        formatEpochMillis(candidate.getDecidedAt()),
                        CsvExportWriter.cell(candidate.getCreatedInspectionId())))
                .toList();
        return buildResponse(ExportType.PREVENTIVE_CANDIDATES, headers, rows);
    }

    private static CsvExportResponse buildResponse(
            ExportType exportType,
            List<String> headers,
            List<List<String>> rows) {
        return new CsvExportResponse(CsvExportWriter.write(headers, new ArrayList<>(rows)), exportType.getFilename());
    }

    private static String categoryName(Asset asset) {
        return asset.getAssetCategory() != null ? asset.getAssetCategory().getName() : null;
    }

    private static String departmentName(Asset asset) {
        return asset.getDepartment() != null ? asset.getDepartment().getName() : null;
    }

    private static String templateName(Inspection inspection) {
        return inspection.getInspectionTemplate() != null
                ? inspection.getInspectionTemplate().getName()
                : null;
    }

    private static String formatEpochMillis(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return ISO_DATE_TIME.format(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC));
    }

    private static String formatLocalDate(java.time.LocalDate date) {
        return date == null ? null : ISO_DATE.format(date);
    }

    private static String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : ISO_DATE_TIME.format(dateTime);
    }

    private static LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    private static LocalDateTime toLocalDateTimeExclusive(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}

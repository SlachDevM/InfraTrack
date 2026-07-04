package com.infratrack.reporting;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.organization.policy.reporting.ReportingPolicyService;
import com.infratrack.preventivemaintenance.PreventiveExecutionCandidate;
import com.infratrack.preventivemaintenance.PreventiveExecutionCandidateRepository;
import com.infratrack.user.UserNameLookup;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportingExportService {

    static final int MAX_EXPORT_WINDOW_DAYS = 365;
    static final String EXPORT_WINDOW_EXCEEDED_MESSAGE =
            "Reporting exports cannot span more than 365 days.";

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
    private final ReportingPolicyService reportingPolicyService;

    public ReportingExportService(
            ReportingAuthorizationService authorizationService,
            AssetRepository assetRepository,
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            WorkOrderRepository workOrderRepository,
            PreventiveExecutionCandidateRepository preventiveExecutionCandidateRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            UserNameLookup userNameLookup,
            ReportingPolicyService reportingPolicyService) {
        this.authorizationService = authorizationService;
        this.assetRepository = assetRepository;
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.workOrderRepository = workOrderRepository;
        this.preventiveExecutionCandidateRepository = preventiveExecutionCandidateRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.userNameLookup = userNameLookup;
        this.reportingPolicyService = reportingPolicyService;
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportAssets(Long userId, Long from, Long to) {
        return buildCsvResponse(ExportType.ASSETS, loadAssetsExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportAssetsXlsx(Long userId, Long from, Long to) {
        return buildXlsxResponse(ExportType.ASSETS, loadAssetsExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportAssetsPdf(Long userId, Long from, Long to) {
        return buildPdfResponse(ExportType.ASSETS, loadAssetsExport(userId, from, to), from, to);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportInspections(Long userId, Long from, Long to) {
        return buildCsvResponse(ExportType.INSPECTIONS, loadInspectionsExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportInspectionsXlsx(Long userId, Long from, Long to) {
        return buildXlsxResponse(ExportType.INSPECTIONS, loadInspectionsExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportInspectionsPdf(Long userId, Long from, Long to) {
        return buildPdfResponse(ExportType.INSPECTIONS, loadInspectionsExport(userId, from, to), from, to);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportIssues(Long userId, Long from, Long to) {
        return buildCsvResponse(ExportType.ISSUES, loadIssuesExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportIssuesXlsx(Long userId, Long from, Long to) {
        return buildXlsxResponse(ExportType.ISSUES, loadIssuesExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportIssuesPdf(Long userId, Long from, Long to) {
        return buildPdfResponse(ExportType.ISSUES, loadIssuesExport(userId, from, to), from, to);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportWorkOrders(Long userId, Long from, Long to) {
        return buildCsvResponse(ExportType.WORK_ORDERS, loadWorkOrdersExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportWorkOrdersXlsx(Long userId, Long from, Long to) {
        return buildXlsxResponse(ExportType.WORK_ORDERS, loadWorkOrdersExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportWorkOrdersPdf(Long userId, Long from, Long to) {
        return buildPdfResponse(ExportType.WORK_ORDERS, loadWorkOrdersExport(userId, from, to), from, to);
    }

    @Transactional(readOnly = true)
    public CsvExportResponse exportPreventiveCandidates(Long userId, Long from, Long to) {
        return buildCsvResponse(ExportType.PREVENTIVE_CANDIDATES, loadPreventiveCandidatesExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportPreventiveCandidatesXlsx(Long userId, Long from, Long to) {
        return buildXlsxResponse(ExportType.PREVENTIVE_CANDIDATES, loadPreventiveCandidatesExport(userId, from, to));
    }

    @Transactional(readOnly = true)
    public ExportFileResponse exportPreventiveCandidatesPdf(Long userId, Long from, Long to) {
        return buildPdfResponse(
                ExportType.PREVENTIVE_CANDIDATES,
                loadPreventiveCandidatesExport(userId, from, to),
                from,
                to);
    }

    private TabularExport loadAssetsExport(Long userId, Long from, Long to) {
        validateExportDateWindow(from, to);
        ExportScope scope = authorizationService.resolveScope(userId);
        List<Asset> assets = assetRepository.findForExport(scope.departmentId(), from, to);
        List<String> headers = List.of(
                "Asset ID", "Asset Name", "Category", "Department", "Location", "Status", "Created At");
        List<List<String>> rows = assets.stream()
                .map(asset -> List.of(
                        ExportCellFormatter.cell(asset.getId()),
                        ExportCellFormatter.cell(asset.getName()),
                        ExportCellFormatter.cell(categoryName(asset)),
                        ExportCellFormatter.cell(departmentName(asset)),
                        ExportCellFormatter.cell(asset.getLocation()),
                        ExportCellFormatter.cell(asset.getStatus()),
                        formatEpochMillis(asset.getCreatedAt())))
                .toList();
        return new TabularExport(headers, rows);
    }

    private TabularExport loadInspectionsExport(Long userId, Long from, Long to) {
        validateExportDateWindow(from, to);
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
                        ExportCellFormatter.cell(inspection.getId()),
                        ExportCellFormatter.cell(inspection.getAsset().getName()),
                        ExportCellFormatter.cell(departmentName(inspection.getAsset())),
                        ExportCellFormatter.cell(inspection.getStatus()),
                        ExportCellFormatter.cell(inspection.getPriority()),
                        ExportCellFormatter.cell(assigneeNames.get(inspection.getAssignedToUserId())),
                        formatLocalDate(inspection.getExpectedCompletionDate()),
                        formatLocalDateTime(inspection.getCompletedAt()),
                        ExportCellFormatter.cell(templateName(inspection)),
                        ExportCellFormatter.cell(inspection.isIssueIdentified())))
                .toList();
        return new TabularExport(headers, rows);
    }

    private TabularExport loadIssuesExport(Long userId, Long from, Long to) {
        validateExportDateWindow(from, to);
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
                        ExportCellFormatter.cell(issue.getId()),
                        ExportCellFormatter.cell(issue.getAsset().getName()),
                        ExportCellFormatter.cell(departmentName(issue.getAsset())),
                        ExportCellFormatter.cell(issue.getIssueType()),
                        ExportCellFormatter.cell(issue.getSeverity()),
                        ExportCellFormatter.cell(issue.getDescription()),
                        ExportCellFormatter.cell(recorderNames.get(issue.getRecordedByUserId())),
                        formatLocalDateTime(issue.getRecordedAt()),
                        ExportCellFormatter.cell(resolvedIssueIds.contains(issue.getId()))))
                .toList();
        return new TabularExport(headers, rows);
    }

    private TabularExport loadWorkOrdersExport(Long userId, Long from, Long to) {
        validateExportDateWindow(from, to);
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
                        ExportCellFormatter.cell(workOrder.getId()),
                        ExportCellFormatter.cell(workOrder.getAsset().getName()),
                        ExportCellFormatter.cell(departmentName(workOrder.getAsset())),
                        ExportCellFormatter.cell(workOrder.getStatus()),
                        ExportCellFormatter.cell(workOrder.getPriority()),
                        ExportCellFormatter.cell(assigneeNames.get(workOrder.getAssignedToUserId())),
                        ExportCellFormatter.cell(workOrder.getDescription()),
                        formatEpochMillis(workOrder.getCreatedAt()),
                        formatEpochMillis(workOrder.getUpdatedAt())))
                .toList();
        return new TabularExport(headers, rows);
    }

    private TabularExport loadPreventiveCandidatesExport(Long userId, Long from, Long to) {
        validateExportDateWindow(from, to);
        ExportScope scope = authorizationService.resolveScope(userId);
        List<PreventiveExecutionCandidate> candidates = preventiveExecutionCandidateRepository.findForExport(
                scope.departmentId(), from, to);
        List<String> headers = List.of(
                "Candidate ID", "Plan Code", "Asset Name", "Department", "Status", "Target Action",
                "Trigger Type", "Evaluated At", "Decision Date", "Created Inspection ID");
        List<List<String>> rows = candidates.stream()
                .map(candidate -> List.of(
                        ExportCellFormatter.cell(candidate.getId()),
                        ExportCellFormatter.cell(candidate.getPlanCodeSnapshot()),
                        ExportCellFormatter.cell(candidate.getAsset().getName()),
                        ExportCellFormatter.cell(departmentName(candidate.getAsset())),
                        ExportCellFormatter.cell(candidate.getCandidateStatus()),
                        ExportCellFormatter.cell(candidate.getTargetActionSnapshot()),
                        ExportCellFormatter.cell(candidate.getTriggerType()),
                        formatEpochMillis(candidate.getEvaluatedAt()),
                        formatEpochMillis(candidate.getDecidedAt()),
                        ExportCellFormatter.cell(candidate.getCreatedInspectionId())))
                .toList();
        return new TabularExport(headers, rows);
    }

    private CsvExportResponse buildCsvResponse(ExportType exportType, TabularExport tabularExport) {
        return new CsvExportResponse(
                CsvExportWriter.write(tabularExport.headers(), new ArrayList<>(tabularExport.rows())),
                exportType.getFilename(reportingPolicyService.getPolicy().defaultExportFormat()));
    }

    private static ExportFileResponse buildXlsxResponse(ExportType exportType, TabularExport tabularExport) {
        return new ExportFileResponse(
                XlsxExportWriter.write(
                        exportType.getSheetName(),
                        tabularExport.headers(),
                        new ArrayList<>(tabularExport.rows())),
                exportType.getFilename(ReportingExportFormat.XLSX));
    }

    private static ExportFileResponse buildPdfResponse(
            ExportType exportType, TabularExport tabularExport, Long from, Long to) {
        return new ExportFileResponse(
                PdfExportWriter.write(
                        exportType.getReportTitle(),
                        tabularExport.headers(),
                        new ArrayList<>(tabularExport.rows()),
                        from,
                        to),
                exportType.getFilename(ReportingExportFormat.PDF));
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

    static void validateExportDateWindow(Long from, Long to) {
        if (from == null || to == null) {
            return;
        }
        LocalDate fromDate = Instant.ofEpochMilli(from).atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate toDate = Instant.ofEpochMilli(to).atZone(ZoneOffset.UTC).toLocalDate();
        long inclusiveDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (inclusiveDays > MAX_EXPORT_WINDOW_DAYS) {
            throw new BusinessValidationException(EXPORT_WINDOW_EXCEEDED_MESSAGE);
        }
    }
}

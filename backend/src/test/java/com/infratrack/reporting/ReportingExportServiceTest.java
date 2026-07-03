package com.infratrack.reporting;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.IssueRepository;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.preventivemaintenance.PreventiveExecutionCandidateRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrderRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingExportServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private PreventiveExecutionCandidateRepository preventiveExecutionCandidateRepository;

    @Mock
    private OperationalDecisionRepository operationalDecisionRepository;

    @Mock
    private UserNameLookup userNameLookup;

    private ReportingExportService exportService;

    @BeforeEach
    void setUp() {
        ReportingAuthorizationService authorizationService = new ReportingAuthorizationService(userService);
        exportService = new ReportingExportService(
                authorizationService,
                assetRepository,
                inspectionRepository,
                issueRepository,
                workOrderRepository,
                preventiveExecutionCandidateRepository,
                operationalDecisionRepository,
                userNameLookup);
    }

    @Test
    void exportAssets_admin_returnsCsvWithHeaderAndRows() {
        when(userService.getById(1L)).thenReturn(admin());
        when(assetRepository.findForExport(isNull(), isNull(), isNull()))
                .thenReturn(List.of(parksAsset()));

        CsvExportResponse response = exportService.exportAssets(1L, null, null);

        assertThat(response.filename()).isEqualTo("assets-export.csv");
        String csv = new String(response.content(), StandardCharsets.UTF_8);
        assertThat(csv).startsWith("Asset ID,Asset Name,Category,Department,Location,Status,Created At\n");
        assertThat(csv).contains("Street Light 001");
        assertThat(csv).contains("Street Lighting");
        assertThat(csv).contains("Parks");
        verify(assetRepository).findForExport(null, null, null);
    }

    @Test
    void exportAssets_manager_scopesToDepartment() {
        when(userService.getById(2L)).thenReturn(manager(3L));
        when(assetRepository.findForExport(eq(3L), isNull(), isNull())).thenReturn(List.of());

        exportService.exportAssets(2L, null, null);

        verify(assetRepository).findForExport(3L, null, null);
        verify(assetRepository, never()).findForExport(isNull(), any(), any());
    }

    @Test
    void exportAssets_emptyResult_returnsHeaderOnly() {
        when(userService.getById(1L)).thenReturn(admin());
        when(assetRepository.findForExport(isNull(), isNull(), isNull())).thenReturn(List.of());

        CsvExportResponse response = exportService.exportAssets(1L, null, null);

        String csv = new String(response.content(), StandardCharsets.UTF_8);
        assertThat(csv.lines()).hasSize(1);
    }

    @Test
    void exportAssets_doesNotPersistData() {
        when(userService.getById(1L)).thenReturn(admin());
        when(assetRepository.findForExport(isNull(), isNull(), isNull())).thenReturn(List.of());

        exportService.exportAssets(1L, null, null);

        verify(assetRepository, never()).save(any());
        verify(assetRepository, never()).delete(any());
        verify(inspectionRepository, never()).save(any());
        verify(issueRepository, never()).save(any());
        verify(workOrderRepository, never()).save(any());
        verify(preventiveExecutionCandidateRepository, never()).save(any());
    }

    @Test
    void exportIssues_resolvedColumnReflectsOperationalDecision() {
        when(userService.getById(1L)).thenReturn(admin());
        when(issueRepository.findForExport(isNull(), isNull(), isNull())).thenReturn(List.of());
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of());

        CsvExportResponse response = exportService.exportIssues(1L, null, null);

        String csv = new String(response.content(), StandardCharsets.UTF_8);
        assertThat(csv).startsWith("Issue ID,Asset Name,Department,Issue Type,Severity,Description,Recorded By,Recorded At,Resolved\n");
    }

    @Test
    void exportAssetsXlsx_admin_containsHeaderAndDataRow() throws Exception {
        when(userService.getById(1L)).thenReturn(admin());
        when(assetRepository.findForExport(isNull(), isNull(), isNull()))
                .thenReturn(List.of(parksAsset()));

        ExportFileResponse response = exportService.exportAssetsXlsx(1L, null, null);

        assertThat(response.filename()).isEqualTo("assets-export.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getSheetName()).isEqualTo("Assets");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Asset ID");
            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("5");
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("Street Light 001");
            assertThat(dataRow.getCell(2).getStringCellValue()).isEqualTo("Street Lighting");
            assertThat(dataRow.getCell(3).getStringCellValue()).isEqualTo("Parks");
        }
    }

    @Test
    void exportAssetsXlsx_manager_scopesToDepartment() {
        when(userService.getById(2L)).thenReturn(manager(3L));
        when(assetRepository.findForExport(eq(3L), isNull(), isNull())).thenReturn(List.of());

        exportService.exportAssetsXlsx(2L, null, null);

        verify(assetRepository).findForExport(3L, null, null);
        verify(assetRepository, never()).findForExport(isNull(), any(), any());
    }

    @Test
    void exportAssetsXlsx_emptyResult_containsHeaderOnly() throws Exception {
        when(userService.getById(1L)).thenReturn(admin());
        when(assetRepository.findForExport(isNull(), isNull(), isNull())).thenReturn(List.of());

        ExportFileResponse response = exportService.exportAssetsXlsx(1L, null, null);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.content()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    private static User admin() {
        User user = new User("admin@test.com", "password", "Admin", UserRole.ADMINISTRATOR);
        user.setId(1L);
        return user;
    }

    private static User manager(Long departmentId) {
        User user = new User("manager@test.com", "password", "Manager", UserRole.MANAGER);
        user.setId(2L);
        Department department = new Department("Parks");
        department.setId(departmentId);
        user.setDepartment(department);
        return user;
    }

    private static Asset parksAsset() {
        Department department = new Department("Parks");
        department.setId(3L);
        AssetCategory category = new AssetCategory("Street Lighting");
        category.setId(10L);
        Asset asset = new Asset(
                "Street Light 001",
                department,
                category,
                "Main Street / Oak Avenue",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 1, 15),
                1L);
        asset.setId(5L);
        return asset;
    }
}

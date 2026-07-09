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
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.reporting.CsvExportResponse;
import com.infratrack.reporting.ReportingExportService;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ReportingExportPostgreSqlIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private BusinessTriggerRepository businessTriggerRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ReportingExportService reportingExportService;

    private Long adminUserId;
    private Long managerUserId;
    private Long departmentId;
    private Long otherDepartmentId;
    private Inspection inRangeInspection;
    private Inspection otherDepartmentInspection;
    private Inspection outOfRangeInspection;
    private Issue inRangeIssue;
    private Issue boundaryIssue;
    private long exportFrom;
    private long exportTo;

    @BeforeEach
    void setUp() {
        Department department = departmentRepository.save(new Department("Export Dept " + System.nanoTime()));
        Department otherDepartment = departmentRepository.save(new Department("Other Dept " + System.nanoTime()));
        AssetCategory category = assetCategoryRepository.save(new AssetCategory("Export Cat " + System.nanoTime()));

        User admin = userRepository.save(buildUser(
                "admin+" + System.nanoTime() + "@export.test",
                "Export Admin",
                UserRole.ADMINISTRATOR));
        User manager = buildUser(
                "manager+" + System.nanoTime() + "@export.test",
                "Export Manager",
                UserRole.MANAGER);
        manager.setDepartment(department);
        manager = userRepository.save(manager);

        adminUserId = admin.getId();
        managerUserId = manager.getId();
        departmentId = department.getId();
        otherDepartmentId = otherDepartment.getId();

        exportFrom = LocalDate.of(2026, 3, 1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        exportTo = LocalDate.of(2026, 3, 31).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

        Asset departmentAsset = assetRepository.save(new Asset(
                "Export Asset",
                department,
                category,
                "Export Street",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 1, 1),
                managerUserId));
        Asset otherDepartmentAsset = assetRepository.save(new Asset(
                "Other Asset",
                otherDepartment,
                category,
                "Other Street",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 1, 1),
                managerUserId));

        BusinessTrigger trigger = businessTriggerRepository.save(new BusinessTrigger(
                departmentAsset,
                BusinessTriggerType.SCHEDULED_INSPECTION,
                "Routine check",
                false,
                managerUserId));

        inRangeInspection = inspectionRepository.saveAndFlush(buildInspection(
                departmentAsset,
                trigger,
                managerUserId,
                toEpochMillis(LocalDateTime.of(2026, 3, 10, 12, 0))));

        outOfRangeInspection = inspectionRepository.saveAndFlush(buildInspection(
                departmentAsset,
                trigger,
                managerUserId,
                toEpochMillis(LocalDateTime.of(2026, 4, 5, 12, 0))));

        otherDepartmentInspection = inspectionRepository.saveAndFlush(buildInspection(
                otherDepartmentAsset,
                businessTriggerRepository.save(new BusinessTrigger(
                        otherDepartmentAsset,
                        BusinessTriggerType.SCHEDULED_INSPECTION,
                        "Other check",
                        false,
                        managerUserId)),
                managerUserId,
                toEpochMillis(LocalDateTime.of(2026, 3, 12, 12, 0))));

        Inspection boundaryInspection = inspectionRepository.saveAndFlush(buildInspection(
                departmentAsset,
                trigger,
                managerUserId,
                toEpochMillis(LocalDateTime.of(2026, 3, 20, 12, 0))));

        inRangeIssue = issueRepository.save(new Issue(
                inRangeInspection,
                departmentAsset,
                "In-range issue",
                IssueSeverity.MEDIUM,
                managerUserId,
                LocalDateTime.of(2026, 3, 15, 10, 0)));

        boundaryIssue = issueRepository.save(new Issue(
                boundaryInspection,
                departmentAsset,
                "Boundary issue",
                IssueSeverity.LOW,
                managerUserId,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(exportTo), ZoneOffset.UTC)));

        issueRepository.save(new Issue(
                outOfRangeInspection,
                departmentAsset,
                "Out-of-range issue",
                IssueSeverity.HIGH,
                managerUserId,
                LocalDateTime.of(2026, 4, 1, 0, 0)));
    }

    @Test
    void issueRepository_findForExport_onPostgreSql_doesNotFailWithUntypedNullParameter() {
        LocalDateTime from = toLocalDateTime(exportFrom);
        LocalDateTime to = toLocalDateTime(exportTo);

        assertThatCode(() -> issueRepository.findForExport(null, from, to))
                .doesNotThrowAnyException();

        assertThat(issueRepository.findForExport(null, from, to))
                .extracting(Issue::getDescription)
                .containsExactlyInAnyOrder("Boundary issue", "In-range issue");
    }

    @Test
    void issueRepository_findForExport_scopesToDepartment() {
        LocalDateTime from = toLocalDateTime(exportFrom);
        LocalDateTime to = toLocalDateTime(exportTo);

        assertThat(issueRepository.findForExport(departmentId, from, to))
                .extracting(Issue::getDescription)
                .containsExactlyInAnyOrder("Boundary issue", "In-range issue");
    }

    @Test
    void issueRepository_findForExport_includesInclusiveToBoundary() {
        LocalDateTime from = toLocalDateTime(exportFrom);
        LocalDateTime to = toLocalDateTime(exportTo);

        assertThat(issueRepository.findForExport(null, from, to))
                .extracting(Issue::getId)
                .contains(boundaryIssue.getId());
    }

    @Test
    void inspectionRepository_findForExport_onPostgreSql_doesNotFailWithUntypedNullParameter() {
        assertThatCode(() -> inspectionRepository.findForExport(null, exportFrom, exportTo))
                .doesNotThrowAnyException();

        assertThat(inspectionRepository.findForExport(null, exportFrom, exportTo))
                .extracting(Inspection::getId)
                .contains(inRangeInspection.getId(), otherDepartmentInspection.getId())
                .doesNotContain(outOfRangeInspection.getId());
    }

    @Test
    void inspectionRepository_findForExport_scopesToDepartment() {
        assertThat(inspectionRepository.findForExport(departmentId, exportFrom, exportTo))
                .extracting(Inspection::getId)
                .contains(inRangeInspection.getId())
                .doesNotContain(otherDepartmentInspection.getId(), outOfRangeInspection.getId());
    }

    @Test
    void inspectionRepository_findForExport_includesInclusiveToBoundary() {
        Inspection boundaryInspection = inspectionRepository.save(buildInspection(
                inRangeInspection.getAsset(),
                inRangeInspection.getBusinessTrigger(),
                managerUserId,
                exportTo));

        assertThat(inspectionRepository.findForExport(null, exportFrom, exportTo))
                .extracting(Inspection::getId)
                .contains(boundaryInspection.getId());
    }

    @Test
    void reportingExportService_exportIssuesCsv_onPostgreSql_succeeds() {
        CsvExportResponse response = reportingExportService.exportIssues(adminUserId, exportFrom, exportTo);

        String csv = new String(response.content(), StandardCharsets.UTF_8);
        assertThat(response.filename()).isEqualTo("issues-export.csv");
        assertThat(csv).contains("In-range issue");
        assertThat(csv).contains("Boundary issue");
        assertThat(csv).doesNotContain("Out-of-range issue");
    }

    private Inspection buildInspection(Asset asset, BusinessTrigger trigger, Long userId, long createdAt) {
        Inspection inspection = new Inspection(
                asset,
                trigger,
                userId,
                userId,
                InspectionPriority.NORMAL,
                LocalDate.of(2026, 3, 20));
        inspection.complete(
                PhysicalCondition.GOOD,
                "Completed for export test",
                true,
                LocalDateTime.of(2026, 3, 20, 15, 0),
                userId);
        setCreatedAt(inspection, createdAt);
        return inspection;
    }

    private static void setCreatedAt(Inspection inspection, long createdAt) {
        try {
            var field = Inspection.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(inspection, createdAt);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to set inspection createdAt for test", exception);
        }
    }

    private static long toEpochMillis(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private static LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    private User buildUser(String email, String name, UserRole role) {
        User user = new User(email, passwordEncoder.encode("Secret1!"), name, role);
        user.setEnabled(true);
        return user;
    }
}

package com.infratrack.reporting;

import com.infratrack.config.GlobalExceptionHandler;
import com.infratrack.config.SecurityConfig;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.security.JwtAuthenticationFilter;
import com.infratrack.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportingExportController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportingExportControllerTest {

    private static final Long ADMIN_USER_ID = 1L;
    private static final Long FIELD_USER_ID = 20L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ReportingExportService exportService;

    @Test
    void exportAssets_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/reporting/exports/assets.csv"))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportAssets_admin_returnsCsvWithHeaders() throws Exception {
        byte[] csv = "Asset ID,Asset Name\n1,Test Asset\n".getBytes(StandardCharsets.UTF_8);
        when(exportService.exportAssets(eq(ADMIN_USER_ID), isNull(), isNull()))
                .thenReturn(new CsvExportResponse(csv, "assets-export.csv"));

        mockMvc.perform(get("/api/reporting/exports/assets.csv")
                        .header("Authorization", bearerToken(ADMIN_USER_ID, "admin@test.com")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"assets-export.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().bytes(csv));
    }

    @Test
    void exportInspections_fieldEmployee_returnsForbidden() throws Exception {
        when(exportService.exportInspections(eq(FIELD_USER_ID), isNull(), isNull()))
                .thenThrow(new ForbiddenOperationException("You do not have permission to export operational reports."));

        mockMvc.perform(get("/api/reporting/exports/inspections.csv")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to export operational reports."));
    }

    @Test
    void exportWorkOrders_passesDateFilters() throws Exception {
        byte[] csv = "Work Order ID\n".getBytes(StandardCharsets.UTF_8);
        when(exportService.exportWorkOrders(eq(ADMIN_USER_ID), eq(1000L), eq(2000L)))
                .thenReturn(new CsvExportResponse(csv, "work-orders-export.csv"));

        mockMvc.perform(get("/api/reporting/exports/work-orders.csv")
                        .param("from", "1000")
                        .param("to", "2000")
                        .header("Authorization", bearerToken(ADMIN_USER_ID, "admin@test.com")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"work-orders-export.csv\""));
    }

    @Test
    void exportAssetsXlsx_admin_returnsXlsxWithHeaders() throws Exception {
        byte[] xlsx = new byte[] {1, 2, 3};
        when(exportService.exportAssetsXlsx(eq(ADMIN_USER_ID), isNull(), isNull()))
                .thenReturn(new ExportFileResponse(xlsx, "assets-export.xlsx"));

        mockMvc.perform(get("/api/reporting/exports/assets.xlsx")
                        .header("Authorization", bearerToken(ADMIN_USER_ID, "admin@test.com")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"assets-export.xlsx\""))
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(xlsx));
    }

    @Test
    void exportInspectionsXlsx_fieldEmployee_returnsForbidden() throws Exception {
        when(exportService.exportInspectionsXlsx(eq(FIELD_USER_ID), isNull(), isNull()))
                .thenThrow(new ForbiddenOperationException("You do not have permission to export operational reports."));

        mockMvc.perform(get("/api/reporting/exports/inspections.xlsx")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to export operational reports."));
    }

    private String bearerToken(Long userId, String email) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, email);
    }
}

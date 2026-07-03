package com.infratrack.reporting;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reporting/exports")
@Tag(name = "Reporting Exports", description = "Read-only CSV and XLSX exports for operational reporting (V2.2.x / V2.3.x)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class ReportingExportController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReportingExportService exportService;

    public ReportingExportController(ReportingExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping(value = "/assets.csv", produces = "text/csv")
    @Operation(summary = "Export assets as CSV")
    @ApiResponse(responseCode = "200", description = "CSV file download")
    public ResponseEntity<byte[]> exportAssets(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return csvResponse(exportService.exportAssets(userId(authentication), from, to));
    }

    @GetMapping(value = "/assets.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export assets as XLSX")
    @ApiResponse(responseCode = "200", description = "XLSX file download")
    public ResponseEntity<byte[]> exportAssetsXlsx(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return xlsxResponse(exportService.exportAssetsXlsx(userId(authentication), from, to));
    }

    @GetMapping(value = "/inspections.csv", produces = "text/csv")
    @Operation(summary = "Export inspections as CSV")
    @ApiResponse(responseCode = "200", description = "CSV file download")
    public ResponseEntity<byte[]> exportInspections(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return csvResponse(exportService.exportInspections(userId(authentication), from, to));
    }

    @GetMapping(value = "/inspections.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export inspections as XLSX")
    @ApiResponse(responseCode = "200", description = "XLSX file download")
    public ResponseEntity<byte[]> exportInspectionsXlsx(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return xlsxResponse(exportService.exportInspectionsXlsx(userId(authentication), from, to));
    }

    @GetMapping(value = "/issues.csv", produces = "text/csv")
    @Operation(summary = "Export issues as CSV")
    @ApiResponse(responseCode = "200", description = "CSV file download")
    public ResponseEntity<byte[]> exportIssues(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return csvResponse(exportService.exportIssues(userId(authentication), from, to));
    }

    @GetMapping(value = "/issues.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export issues as XLSX")
    @ApiResponse(responseCode = "200", description = "XLSX file download")
    public ResponseEntity<byte[]> exportIssuesXlsx(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return xlsxResponse(exportService.exportIssuesXlsx(userId(authentication), from, to));
    }

    @GetMapping(value = "/work-orders.csv", produces = "text/csv")
    @Operation(summary = "Export work orders as CSV")
    @ApiResponse(responseCode = "200", description = "CSV file download")
    public ResponseEntity<byte[]> exportWorkOrders(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return csvResponse(exportService.exportWorkOrders(userId(authentication), from, to));
    }

    @GetMapping(value = "/work-orders.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export work orders as XLSX")
    @ApiResponse(responseCode = "200", description = "XLSX file download")
    public ResponseEntity<byte[]> exportWorkOrdersXlsx(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return xlsxResponse(exportService.exportWorkOrdersXlsx(userId(authentication), from, to));
    }

    @GetMapping(value = "/preventive-candidates.csv", produces = "text/csv")
    @Operation(summary = "Export preventive execution candidates as CSV")
    @ApiResponse(responseCode = "200", description = "CSV file download")
    public ResponseEntity<byte[]> exportPreventiveCandidates(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return csvResponse(exportService.exportPreventiveCandidates(userId(authentication), from, to));
    }

    @GetMapping(value = "/preventive-candidates.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export preventive execution candidates as XLSX")
    @ApiResponse(responseCode = "200", description = "XLSX file download")
    public ResponseEntity<byte[]> exportPreventiveCandidatesXlsx(
            Authentication authentication,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return xlsxResponse(exportService.exportPreventiveCandidatesXlsx(userId(authentication), from, to));
    }

    private static Long userId(Authentication authentication) {
        return ((JwtAuthenticationToken) authentication).getUserId();
    }

    private static ResponseEntity<byte[]> csvResponse(CsvExportResponse export) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + export.filename() + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(export.content());
    }

    private static ResponseEntity<byte[]> xlsxResponse(ExportFileResponse export) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + export.filename() + "\"")
                .contentType(XLSX_MEDIA_TYPE)
                .body(export.content());
    }
}

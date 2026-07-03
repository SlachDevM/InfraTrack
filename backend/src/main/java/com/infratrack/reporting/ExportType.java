package com.infratrack.reporting;

public enum ExportType {
    ASSETS("assets-export", "Assets"),
    INSPECTIONS("inspections-export", "Inspections"),
    ISSUES("issues-export", "Issues"),
    WORK_ORDERS("work-orders-export", "Work Orders"),
    PREVENTIVE_CANDIDATES("preventive-candidates-export", "Preventive Candidates");

    private final String baseFilename;
    private final String sheetName;

    ExportType(String baseFilename, String sheetName) {
        this.baseFilename = baseFilename;
        this.sheetName = sheetName;
    }

    public String getFilename() {
        return getFilename(ReportingExportFormat.CSV);
    }

    public String getFilename(ReportingExportFormat format) {
        return baseFilename + format.getExtension();
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getReportTitle() {
        return sheetName + " Export";
    }
}

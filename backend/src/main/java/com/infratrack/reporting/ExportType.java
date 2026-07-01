package com.infratrack.reporting;

public enum ExportType {
    ASSETS("assets-export.csv"),
    INSPECTIONS("inspections-export.csv"),
    ISSUES("issues-export.csv"),
    WORK_ORDERS("work-orders-export.csv"),
    PREVENTIVE_CANDIDATES("preventive-candidates-export.csv");

    private final String filename;

    ExportType(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}

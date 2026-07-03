package com.infratrack.reporting;

public enum ReportingExportFormat {
    CSV(".csv"),
    XLSX(".xlsx");

    private final String extension;

    ReportingExportFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}

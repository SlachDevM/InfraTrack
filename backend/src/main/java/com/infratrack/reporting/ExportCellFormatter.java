package com.infratrack.reporting;

public final class ExportCellFormatter {

    private ExportCellFormatter() {
    }

    public static String cell(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

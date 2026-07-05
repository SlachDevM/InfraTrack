package com.infratrack.reporting;

public final class ExportCellFormatter {

    private ExportCellFormatter() {
    }

    public static String cell(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Prefixes spreadsheet formula injection triggers with a single quote so CSV and XLSX
     * consumers display the literal text instead of evaluating a formula (CWE-1236).
     */
    public static String sanitizeSpreadsheetText(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int index = 0;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        if (index >= value.length()) {
            return value;
        }
        char firstSignificant = value.charAt(index);
        if (firstSignificant == '='
                || firstSignificant == '+'
                || firstSignificant == '-'
                || firstSignificant == '@'
                || firstSignificant == '\t') {
            return "'" + value;
        }
        return value;
    }
}

package com.infratrack.reporting;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvExportWriter {

    private CsvExportWriter() {
    }

    public static byte[] write(List<String> headers, List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append(formatLine(headers));
        for (List<String> row : rows) {
            builder.append(formatLine(row));
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    static String formatLine(List<String> cells) {
        return cells.stream()
                .map(CsvExportWriter::escapeCell)
                .collect(Collectors.joining(","))
                + "\n";
    }

    static String escapeCell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    static String cell(Object value) {
        return ExportCellFormatter.cell(value);
    }
}

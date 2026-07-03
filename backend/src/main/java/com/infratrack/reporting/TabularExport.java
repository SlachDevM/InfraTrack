package com.infratrack.reporting;

import java.util.List;

/**
 * Format-neutral tabular export data shared by CSV and XLSX writers.
 */
public record TabularExport(List<String> headers, List<List<String>> rows) {
}

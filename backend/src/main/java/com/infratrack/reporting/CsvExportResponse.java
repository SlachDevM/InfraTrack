package com.infratrack.reporting;

public record CsvExportResponse(byte[] content, String filename) {
}

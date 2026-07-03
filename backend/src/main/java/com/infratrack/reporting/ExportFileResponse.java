package com.infratrack.reporting;

public record ExportFileResponse(byte[] content, String filename) {
}

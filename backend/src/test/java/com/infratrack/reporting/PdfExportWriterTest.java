package com.infratrack.reporting;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfExportWriterTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-03-15T10:30:00Z");

    @Test
    void write_shouldGenerateNonEmptyPdfBytes() throws Exception {
        byte[] content = PdfExportWriter.write(
                "Assets Export",
                List.of("Asset ID", "Asset Name"),
                List.of(List.of("1", "Street Light 001")),
                null,
                null,
                GENERATED_AT);

        assertThat(content.length).isGreaterThan(100);
        assertThat(new String(content, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void write_shouldIncludeTitleAndGeneratedTimestamp() throws Exception {
        byte[] content = PdfExportWriter.write(
                "Assets Export",
                List.of("Asset ID"),
                List.of(),
                null,
                null,
                GENERATED_AT);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Assets Export");
            assertThat(text).contains("Generated:");
            assertThat(text).contains("2026-03-15T10:30:00");
        }
    }

    @Test
    void write_shouldIncludeDateRangeWhenProvided() throws Exception {
        byte[] content = PdfExportWriter.write(
                "Issues Export",
                List.of("Issue ID"),
                List.of(),
                1_704_067_200_000L,
                1_732_252_800_000L,
                GENERATED_AT);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Date range:");
        }
    }

    @Test
    void write_shouldRenderNullValuesAsBlankCells() throws Exception {
        List<String> row = new ArrayList<>();
        row.add("1");
        row.add(null);
        byte[] content = PdfExportWriter.write(
                "Issues Export",
                List.of("Issue ID", "Description"),
                List.of(row),
                null,
                null,
                GENERATED_AT);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Issue ID");
            assertThat(text).contains("Description");
            assertThat(text).contains("1");
        }
    }

    @Test
    void write_shouldWriteMultipleRows() throws Exception {
        byte[] content = PdfExportWriter.write(
                "Assets Export",
                List.of("Asset ID"),
                List.of(List.of("1"), List.of("2")),
                null,
                null,
                GENERATED_AT);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("1");
            assertThat(text).contains("2");
        }
    }

    @Test
    void write_shouldPreserveReadableSpecialCharacters() throws Exception {
        byte[] content = PdfExportWriter.write(
                "Issues Export",
                List.of("Description"),
                List.of(List.of("Broken \"chain\", needs review urgent")),
                null,
                null,
                GENERATED_AT);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content))) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Broken");
            assertThat(text).contains("chain");
        }
    }

    @Test
    void formatDateRange_withoutFilters_returnsNull() {
        assertThat(PdfExportWriter.formatDateRange(null, null)).isNull();
    }

    @Test
    void sanitizeCell_shouldTruncateLongValues() {
        assertThat(PdfExportWriter.sanitizeCell("x".repeat(80)))
                .endsWith("...")
                .hasSizeLessThanOrEqualTo(48);
    }
}

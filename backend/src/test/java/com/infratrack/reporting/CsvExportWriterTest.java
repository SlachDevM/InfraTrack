package com.infratrack.reporting;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportWriterTest {

    @Test
    void write_shouldIncludeHeaderAndRows() {
        byte[] csv = CsvExportWriter.write(
                List.of("Asset ID", "Asset Name"),
                List.of(List.of("1", "Street Light 001")));

        String content = new String(csv, StandardCharsets.UTF_8);
        assertThat(content).isEqualTo("""
                Asset ID,Asset Name
                1,Street Light 001
                """);
    }

    @Test
    void escapeCell_shouldQuoteCommasQuotesAndNewlines() {
        assertThat(CsvExportWriter.escapeCell(null)).isEmpty();
        assertThat(CsvExportWriter.escapeCell("Park BBQ")).isEqualTo("Park BBQ");
        assertThat(CsvExportWriter.escapeCell("Main St, Oak Ave")).isEqualTo("\"Main St, Oak Ave\"");
        assertThat(CsvExportWriter.escapeCell("He said \"hello\"")).isEqualTo("\"He said \"\"hello\"\"\"");
        assertThat(CsvExportWriter.escapeCell("line1\nline2")).isEqualTo("\"line1\nline2\"");
    }

    @Test
    void write_shouldReturnHeaderOnlyForEmptyRows() {
        byte[] csv = CsvExportWriter.write(List.of("Issue ID"), List.of());

        assertThat(new String(csv, StandardCharsets.UTF_8)).isEqualTo("Issue ID\n");
    }
}

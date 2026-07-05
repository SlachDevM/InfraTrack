package com.infratrack.reporting;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportCellFormatterTest {

    @Test
    void sanitizeSpreadsheetText_shouldPrefixEqualsSign() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("=SUM(1,1)"))
                .isEqualTo("'=SUM(1,1)");
    }

    @Test
    void sanitizeSpreadsheetText_shouldPrefixPlusSign() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("+SUM(1,1)"))
                .isEqualTo("'+SUM(1,1)");
    }

    @Test
    void sanitizeSpreadsheetText_shouldPrefixMinusSign() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("-123"))
                .isEqualTo("'-123");
    }

    @Test
    void sanitizeSpreadsheetText_shouldPrefixAtSign() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("@USER"))
                .isEqualTo("'@USER");
    }

    @Test
    void sanitizeSpreadsheetText_shouldPrefixTabPrefixedValue() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("\t=evil"))
                .isEqualTo("'\t=evil");
    }

    @Test
    void sanitizeSpreadsheetText_shouldPrefixAfterLeadingWhitespace() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("  =HYPERLINK(\"https://evil.example\")"))
                .isEqualTo("'  =HYPERLINK(\"https://evil.example\")");
    }

    @Test
    void sanitizeSpreadsheetText_shouldLeaveNormalTextUnchanged() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("Park BBQ")).isEqualTo("Park BBQ");
    }

    @Test
    void sanitizeSpreadsheetText_shouldLeaveNumericStringsUnchanged() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("123")).isEqualTo("123");
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("3.14")).isEqualTo("3.14");
    }

    @Test
    void sanitizeSpreadsheetText_shouldLeaveNullAndEmptyUnchanged() {
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText(null)).isNull();
        assertThat(ExportCellFormatter.sanitizeSpreadsheetText("")).isEmpty();
    }

    @Test
    void cell_shouldLeaveObjectConversionUnchanged() {
        assertThat(ExportCellFormatter.cell(null)).isNull();
        assertThat(ExportCellFormatter.cell(42)).isEqualTo("42");
        assertThat(ExportCellFormatter.cell("=SUM(1,1)")).isEqualTo("=SUM(1,1)");
    }
}

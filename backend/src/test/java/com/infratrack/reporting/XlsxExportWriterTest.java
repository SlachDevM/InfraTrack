package com.infratrack.reporting;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XlsxExportWriterTest {

    @Test
    void write_shouldCreateHeaderRow() throws Exception {
        byte[] content = XlsxExportWriter.write(
                "Assets",
                List.of("Asset ID", "Asset Name"),
                List.of());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getSheetName()).isEqualTo("Assets");
            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Asset ID");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Asset Name");
            assertThat(sheet.getPaneInformation().isFreezePane()).isTrue();
        }
    }

    @Test
    void write_shouldRenderNullValuesAsBlankCells() throws Exception {
        List<String> row = new ArrayList<>();
        row.add("1");
        row.add(null);
        byte[] content = XlsxExportWriter.write(
                "Issues",
                List.of("Issue ID", "Description"),
                List.of(row));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("1");
            assertThat(dataRow.getCell(1)).isNull();
        }
    }

    @Test
    void write_shouldPreserveSpecialCharacters() throws Exception {
        byte[] content = XlsxExportWriter.write(
                "Issues",
                List.of("Description"),
                List.of(List.of("Broken \"chain\", needs review\nurgent")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo("Broken \"chain\", needs review\nurgent");
        }
    }

    @Test
    void write_shouldWriteMultipleRows() throws Exception {
        byte[] content = XlsxExportWriter.write(
                "Assets",
                List.of("Asset ID"),
                List.of(List.of("1"), List.of("2")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("1");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("2");
        }
    }

    @Test
    void sanitizeSheetName_shouldTruncateLongNames() {
        String sanitized = XlsxExportWriter.sanitizeSheetName("A".repeat(40));

        assertThat(sanitized).hasSize(31);
    }

    @Test
    void write_shouldProtectFormulaInjectionTriggers() throws Exception {
        byte[] content = XlsxExportWriter.write(
                "Issues",
                List.of("Formula", "Normal"),
                List.of(List.of("=SUM(1,1)", "Park BBQ")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Row dataRow = workbook.getSheetAt(0).getRow(1);
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("'=SUM(1,1)");
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("Park BBQ");
        }
    }

    @Test
    void write_shouldProtectPlusMinusAtAndTabPrefixedValues() throws Exception {
        byte[] content = XlsxExportWriter.write(
                "Issues",
                List.of("Value"),
                List.of(
                        List.of("+SUM(1,1)"),
                        List.of("-123"),
                        List.of("@USER"),
                        List.of("\t=evil")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("'+SUM(1,1)");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("'-123");
            assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("'@USER");
            assertThat(sheet.getRow(4).getCell(0).getStringCellValue()).isEqualTo("'\t=evil");
        }
    }

    @Test
    void write_shouldLeaveNumericStringsUnchanged() throws Exception {
        byte[] content = XlsxExportWriter.write(
                "Assets",
                List.of("Asset ID"),
                List.of(List.of("123")));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            assertThat(workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo("123");
        }
    }
}

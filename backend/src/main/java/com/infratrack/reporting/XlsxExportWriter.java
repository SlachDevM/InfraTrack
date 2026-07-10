package com.infratrack.reporting;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class XlsxExportWriter {

    private static final int EXCEL_SHEET_NAME_MAX_LENGTH = 31;
    private static final int STREAMING_ROW_WINDOW_SIZE = 100;

    static final int AUTO_SIZE_MAX_ROWS = 5_000;
    static final int DEFAULT_COLUMN_WIDTH = 15 * 256;

    private XlsxExportWriter() {
    }

    public static byte[] write(String sheetName, List<String> headers, List<List<String>> rows) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_ROW_WINDOW_SIZE);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet(sanitizeSheetName(sheetName));

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                Cell cell = headerRow.createCell(columnIndex);
                cell.setCellValue(ExportCellFormatter.sanitizeSpreadsheetText(headers.get(columnIndex)));
                cell.setCellStyle(headerStyle);
            }

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Row dataRow = sheet.createRow(rowIndex + 1);
                List<String> rowValues = rows.get(rowIndex);
                for (int columnIndex = 0; columnIndex < rowValues.size(); columnIndex++) {
                    String value = rowValues.get(columnIndex);
                    if (value != null) {
                        dataRow.createCell(columnIndex)
                                .setCellValue(ExportCellFormatter.sanitizeSpreadsheetText(value));
                    }
                }
            }

            sheet.createFreezePane(0, 1);
            if (rows.size() <= AUTO_SIZE_MAX_ROWS) {
                if (sheet instanceof SXSSFSheet streamingSheet) {
                    for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                        streamingSheet.trackColumnForAutoSizing(columnIndex);
                    }
                }
                for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                    sheet.autoSizeColumn(columnIndex);
                }
            } else {
                for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                    sheet.setColumnWidth(columnIndex, DEFAULT_COLUMN_WIDTH);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate XLSX export", ex);
        }
    }

    static String sanitizeSheetName(String sheetName) {
        if (sheetName == null || sheetName.isBlank()) {
            return "Export";
        }
        String sanitized = sheetName
                .replace('\\', ' ')
                .replace('/', ' ')
                .replace('*', ' ')
                .replace('[', ' ')
                .replace(']', ' ')
                .replace(':', ' ')
                .replace('?', ' ');
        if (sanitized.length() > EXCEL_SHEET_NAME_MAX_LENGTH) {
            return sanitized.substring(0, EXCEL_SHEET_NAME_MAX_LENGTH);
        }
        return sanitized;
    }
}

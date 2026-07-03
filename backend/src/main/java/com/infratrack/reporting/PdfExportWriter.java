package com.infratrack.reporting;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PdfExportWriter {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final float MARGIN = 36f;
    private static final float TITLE_FONT_SIZE = 14f;
    private static final float META_FONT_SIZE = 9f;
    private static final float TABLE_FONT_SIZE = 8f;
    private static final float ROW_HEIGHT = 14f;
    private static final float FOOTER_HEIGHT = 20f;
    private static final int LANDSCAPE_COLUMN_THRESHOLD = 6;
    private static final int MAX_CELL_CHARACTERS = 48;

    private PdfExportWriter() {
    }

    public static byte[] write(
            String title,
            List<String> headers,
            List<List<String>> rows,
            Long fromEpochMillis,
            Long toEpochMillis) {
        return write(title, headers, rows, fromEpochMillis, toEpochMillis, Instant.now());
    }

    static byte[] write(
            String title,
            List<String> headers,
            List<List<String>> rows,
            Long fromEpochMillis,
            Long toEpochMillis,
            Instant generatedAt) {
        boolean landscape = headers.size() > LANDSCAPE_COLUMN_THRESHOLD;
        PDRectangle pageSize = landscape
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            TableLayout layout = new TableLayout(pageSize, headers.size());
            PdfPageContext pageContext = startPage(document, pageSize, layout, title, generatedAt, fromEpochMillis,
                    toEpochMillis, headers);
            float y = pageContext.y();

            for (List<String> row : rows) {
                if (y < MARGIN + FOOTER_HEIGHT + ROW_HEIGHT) {
                    finishPage(pageContext);
                    pageContext = startPage(document, pageSize, layout, title, generatedAt, fromEpochMillis,
                            toEpochMillis, headers);
                    y = pageContext.y();
                }
                drawDataRow(pageContext, layout, row, y);
                y -= ROW_HEIGHT;
            }

            finishPage(pageContext);
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate PDF export", ex);
        }
    }

    private static PdfPageContext startPage(
            PDDocument document,
            PDRectangle pageSize,
            TableLayout layout,
            String title,
            Instant generatedAt,
            Long fromEpochMillis,
            Long toEpochMillis,
            List<String> headers) throws IOException {
        PDPage page = new PDPage(pageSize);
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        float y = pageSize.getHeight() - MARGIN;

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
        y = drawTextLine(contentStream, title, MARGIN, y, TITLE_FONT_SIZE);

        contentStream.setFont(PDType1Font.HELVETICA, META_FONT_SIZE);
        y = drawTextLine(
                contentStream,
                "Generated: " + ISO_DATE_TIME.format(LocalDateTime.ofInstant(generatedAt, ZoneOffset.UTC)),
                MARGIN,
                y - 4f,
                META_FONT_SIZE);

        String dateRange = formatDateRange(fromEpochMillis, toEpochMillis);
        if (dateRange != null) {
            y = drawTextLine(contentStream, dateRange, MARGIN, y - 2f, META_FONT_SIZE);
        }

        y -= 8f;
        drawHeaderRow(contentStream, layout, headers, y);
        y -= ROW_HEIGHT;

        return new PdfPageContext(document, page, contentStream, layout, y, 1);
    }

    private static void drawHeaderRow(
            PDPageContentStream contentStream,
            TableLayout layout,
            List<String> headers,
            float y) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, TABLE_FONT_SIZE);
        for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
            float x = layout.columnX(columnIndex);
            drawCellText(contentStream, sanitizeCell(headers.get(columnIndex)), x, y);
            contentStream.moveTo(x, y - 2f);
            contentStream.lineTo(x + layout.columnWidth(), y - 2f);
            contentStream.stroke();
        }
    }

    private static void drawDataRow(PdfPageContext pageContext, TableLayout layout, List<String> row, float y)
            throws IOException {
        pageContext.contentStream().setFont(PDType1Font.HELVETICA, TABLE_FONT_SIZE);
        for (int columnIndex = 0; columnIndex < layout.columnCount(); columnIndex++) {
            String value = columnIndex < row.size() ? row.get(columnIndex) : null;
            drawCellText(
                    pageContext.contentStream(),
                    sanitizeCell(value),
                    layout.columnX(columnIndex),
                    y);
        }
    }

    private static void drawCellText(PDPageContentStream contentStream, String text, float x, float y)
            throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private static float drawTextLine(
            PDPageContentStream contentStream,
            String text,
            float x,
            float y,
            float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(sanitizeCell(text));
        contentStream.endText();
        return y - fontSize - 2f;
    }

    private static void finishPage(PdfPageContext pageContext) throws IOException {
        pageContext.contentStream().setFont(PDType1Font.HELVETICA, META_FONT_SIZE);
        String footer = "InfraTrack operational export";
        pageContext.contentStream().beginText();
        pageContext.contentStream().newLineAtOffset(MARGIN, MARGIN);
        pageContext.contentStream().showText(footer);
        pageContext.contentStream().endText();
        pageContext.contentStream().close();
    }

    static String formatDateRange(Long fromEpochMillis, Long toEpochMillis) {
        if (fromEpochMillis == null && toEpochMillis == null) {
            return null;
        }
        String fromLabel = fromEpochMillis == null
                ? "any"
                : ISO_DATE.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(fromEpochMillis), ZoneOffset.UTC));
        String toLabel = toEpochMillis == null
                ? "any"
                : ISO_DATE.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(toEpochMillis), ZoneOffset.UTC));
        return "Date range: " + fromLabel + " to " + toLabel;
    }

    static String sanitizeCell(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ');
        if (normalized.length() > MAX_CELL_CHARACTERS) {
            return normalized.substring(0, MAX_CELL_CHARACTERS - 3) + "...";
        }
        return normalized;
    }

    private record PdfPageContext(
            PDDocument document,
            PDPage page,
            PDPageContentStream contentStream,
            TableLayout layout,
            float y,
            int pageNumber) {
    }

    private record TableLayout(PDRectangle pageSize, int columnCount, float columnWidth) {

        TableLayout(PDRectangle pageSize, int columnCount) {
            this(
                    pageSize,
                    Math.max(columnCount, 1),
                    (pageSize.getWidth() - (2 * MARGIN)) / Math.max(columnCount, 1));
        }

        float columnX(int columnIndex) {
            return MARGIN + (columnIndex * columnWidth);
        }
    }
}

package com.infratrack.operationaldocument;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationalDocumentUploadValidatorTest {

    private OperationalDocumentUploadValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OperationalDocumentUploadValidator();
    }

    @Test
    void validate_shouldAcceptValidPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "manual.pdf",
                "application/pdf",
                "%PDF-1.4\n% fake pdf content".getBytes(StandardCharsets.UTF_8));

        OperationalDocumentUploadValidator.ValidatedUpload validated = validator.validate(file);

        assertThat(validated.sanitizedFileName()).isEqualTo("manual.pdf");
        assertThat(validated.detectedContentType()).isEqualTo("application/pdf");
    }

    @Test
    void validate_shouldAcceptValidPng() {
        byte[] pngHeader = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D
        };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "drawing.png",
                "image/png",
                pngHeader);

        OperationalDocumentUploadValidator.ValidatedUpload validated = validator.validate(file);

        assertThat(validated.sanitizedFileName()).isEqualTo("drawing.png");
        assertThat(validated.detectedContentType()).isEqualTo("image/png");
    }

    @Test
    void validate_shouldAcceptValidJpeg() {
        byte[] jpegHeader = new byte[] {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
        };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                jpegHeader);

        OperationalDocumentUploadValidator.ValidatedUpload validated = validator.validate(file);

        assertThat(validated.sanitizedFileName()).isEqualTo("photo.jpg");
        assertThat(validated.detectedContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void validate_shouldRejectSpoofedExecutableRenamedAsPdf() {
        byte[] executableHeader = new byte[] {
                0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00
        };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                executableHeader);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Invalid document");
    }

    @Test
    void validate_shouldRejectInvalidMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "plain text content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Invalid document");
    }

    @Test
    void validate_shouldRejectEmptyFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "application/pdf",
                "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Invalid document");
    }

    @Test
    void validate_shouldRejectBlankFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "   ",
                "application/pdf",
                "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Invalid document");
    }

    @Test
    void validate_shouldRejectMissingFile() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Document file is required");
    }

    @Test
    void sanitizeFileName_shouldStripPathTraversalAndPreserveExtension() {
        String sanitized = validator.sanitizeFileName("../../etc/passwd/report.pdf");

        assertThat(sanitized).isEqualTo("report.pdf");
    }

    @Test
    void sanitizeFileName_shouldNormaliseWindowsPathSegments() {
        String sanitized = validator.sanitizeFileName("C:\\Users\\docs\\scan.pdf");

        assertThat(sanitized).isEqualTo("scan.pdf");
    }

    @Test
    void sanitizeFileName_shouldReplaceUnsafeCharactersInBaseName() {
        String sanitized = validator.sanitizeFileName("my report (final).pdf");

        assertThat(sanitized).isEqualTo("my_report__final_.pdf");
    }

    @Test
    void validate_shouldRejectPdfExtensionWithPngContent() {
        byte[] pngHeader = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D
        };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                pngHeader);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Invalid document");
    }
}

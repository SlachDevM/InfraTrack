package com.infratrack.operationaldocument;

import com.infratrack.exception.BusinessValidationException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Validates operational document uploads using extension, declared content type and Tika content inspection.
 */
@Component
public class OperationalDocumentUploadValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "png", "jpeg", "jpg", "docx", "xlsx");

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
            "pdf", "application/pdf",
            "png", "image/png",
            "jpeg", "image/jpeg",
            "jpg", "image/jpeg",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final Set<String> BLOCKED_CONTENT_TYPES = Set.of(
            "application/x-msdownload",
            "application/x-msdos-program",
            "application/x-executable",
            "application/vnd.microsoft.portable-executable",
            "application/x-dosexec",
            "application/x-sh",
            "application/x-bat",
            "application/java-archive"
    );

    private final Tika tika = new Tika();

    public ValidatedUpload validate(MultipartFile file) {
        requirePresentFile(file);
        String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(sanitizedFileName);
        String detectedContentType = detectContentType(file, sanitizedFileName);
        rejectBlockedContentTypes(detectedContentType);
        requireAllowedContentType(detectedContentType);
        requireMatchingExtension(extension, detectedContentType);
        return new ValidatedUpload(sanitizedFileName, detectedContentType);
    }

    private void requirePresentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("Document file is required");
        }
    }

    String sanitizeFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessValidationException("Invalid document");
        }

        String normalized = originalFilename.replace('\\', '/').trim();
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            normalized = normalized.substring(lastSlash + 1);
        }

        normalized = normalized.trim();
        if (normalized.isBlank() || ".".equals(normalized) || "..".equals(normalized)) {
            throw new BusinessValidationException("Invalid document");
        }
        if (normalized.contains("..") || normalized.contains("/") || normalized.contains("\\")) {
            throw new BusinessValidationException("Invalid document");
        }

        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == normalized.length() - 1) {
            throw new BusinessValidationException("Invalid document");
        }

        String baseName = normalized.substring(0, dotIndex).replaceAll("[^a-zA-Z0-9._-]", "_");
        String extension = normalized.substring(dotIndex + 1).toLowerCase(Locale.ROOT);

        if (baseName.isBlank() || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessValidationException("Invalid document");
        }

        return baseName + "." + extension;
    }

    private String extractExtension(String sanitizedFileName) {
        return sanitizedFileName.substring(sanitizedFileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String detectContentType(MultipartFile file, String sanitizedFileName) {
        try (InputStream inputStream = file.getInputStream()) {
            String detected = tika.detect(inputStream, sanitizedFileName);
            if (detected == null || detected.isBlank()) {
                throw new BusinessValidationException("Invalid document");
            }
            return detected.toLowerCase(Locale.ROOT);
        } catch (IOException ex) {
            throw new BusinessValidationException("Invalid document");
        }
    }

    private void rejectBlockedContentTypes(String detectedContentType) {
        if (BLOCKED_CONTENT_TYPES.contains(detectedContentType)) {
            throw new BusinessValidationException("Invalid document");
        }
    }

    private void requireAllowedContentType(String detectedContentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(detectedContentType)) {
            throw new BusinessValidationException("Invalid document");
        }
    }

    private void requireMatchingExtension(String extension, String detectedContentType) {
        String expectedContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
        if (expectedContentType == null || !expectedContentType.equals(detectedContentType)) {
            throw new BusinessValidationException("Invalid document");
        }
    }

    public record ValidatedUpload(String sanitizedFileName, String detectedContentType) {
    }
}

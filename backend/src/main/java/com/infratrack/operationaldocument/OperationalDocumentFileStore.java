package com.infratrack.operationaldocument;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class OperationalDocumentFileStore {

    private final Path storageRoot;

    public OperationalDocumentFileStore(
            @Value("${app.operational-documents.storage-path}") String storagePath) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public StoredFileDetails store(MultipartFile file, String contentType) {
        try {
            Files.createDirectories(storageRoot);
            String storedFileName = UUID.randomUUID().toString();
            Path targetPath = storageRoot.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFileDetails(
                    storedFileName,
                    targetPath.toString(),
                    contentType,
                    file.getSize()
            );
        } catch (IOException ex) {
            throw new BusinessValidationException("Invalid document");
        }
    }

    public Resource loadAsResource(String storagePath) {
        try {
            Path filePath = Path.of(storagePath).normalize();
            if (!filePath.startsWith(storageRoot)) {
                throw new NotFoundException("Document not found");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("Document not found");
            }
            return resource;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NotFoundException("Document not found");
        }
    }

    public record StoredFileDetails(
            String storedFileName,
            String storagePath,
            String contentType,
            long fileSize) {
    }
}

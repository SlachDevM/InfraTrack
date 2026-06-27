package com.infratrack.config.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Actuator health contributor verifying the operational document storage directory exists and is writable.
 */
@Component
public class OperationalDocumentStorageHealthIndicator implements HealthIndicator {

    private final Path storageRoot;

    public OperationalDocumentStorageHealthIndicator(
            @Value("${app.operational-documents.storage-path}") String storagePath) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
    }

    @Override
    public Health health() {
        if (!Files.exists(storageRoot)) {
            return Health.down()
                    .withDetail("path", storageRoot.toString())
                    .withDetail("reason", "directory does not exist")
                    .build();
        }
        if (!Files.isDirectory(storageRoot)) {
            return Health.down()
                    .withDetail("path", storageRoot.toString())
                    .withDetail("reason", "path is not a directory")
                    .build();
        }
        if (!Files.isWritable(storageRoot)) {
            return Health.down()
                    .withDetail("path", storageRoot.toString())
                    .withDetail("reason", "directory is not writable")
                    .build();
        }

        try {
            Path probeFile = Files.createTempFile(storageRoot, ".health-", ".probe");
            Files.delete(probeFile);
            return Health.up().withDetail("path", storageRoot.toString()).build();
        } catch (IOException ex) {
            return Health.down()
                    .withDetail("path", storageRoot.toString())
                    .withDetail("reason", "directory is not writable")
                    .build();
        }
    }
}
